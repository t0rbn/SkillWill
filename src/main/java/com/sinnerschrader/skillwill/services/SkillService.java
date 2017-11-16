package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.domain.skills.KnownSkillSuggestionComparator;
import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;
import com.sinnerschrader.skillwill.domain.skills.SkillUtils;
import com.sinnerschrader.skillwill.domain.skills.SuggestionSkill;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Services handling skills management (create, rename, suggest, delete, ...)
 *
 * @author torree
 */
@Service
@EnableRetry
public class SkillService {

  private static final Logger logger = LoggerFactory.getLogger(SkillService.class);

  @Autowired
  private SkillRepository skillRepository;

  @Autowired
  private PersonRepository personRepository;

  private List<KnownSkill> getAllSkills(boolean excludeHidden) {
    return excludeHidden ? skillRepository.findAllExcludeHidden() : skillRepository.findAll();
  }

  private List<KnownSkill> getAutocompleteSkills(String input, boolean excludeHidden) {
    List<KnownSkill> skills = skillRepository.findByNameStemLike(SkillUtils.toStem(input));
    if (excludeHidden) {
      skills = skills.stream().filter(s -> !s.isHidden()).collect(Collectors.toList());
    }
    skills.sort(new KnownSkillSuggestionComparator(input));
    return skills;
  }

  public List<KnownSkill> getSkills(String search, boolean excludeHidden, int count) {
    // count value of -1 means no limiting, so no Integer wrapper is needed,
    List<KnownSkill> found = StringUtils.isEmpty(search) ? getAllSkills(excludeHidden) : getAutocompleteSkills(search, excludeHidden);
    return count < 0 ? found : found.stream().limit(count).collect(Collectors.toList());
  }

  public KnownSkill getSkillByName(String name) {
    return skillRepository.findByName(name);
  }

  public Set<KnownSkill> getSkillsByStemsExcludeHidden(List<String> stems) throws SkillNotFoundException {
    return stems.stream()
      .map(s -> skillRepository.findByNameStem(SkillUtils.toStem(s)))
      .filter(s -> s != null && !s.isHidden())
      .collect(Collectors.toSet());
  }

  private List<SuggestionSkill> aggregateSuggestions(Collection<KnownSkill> skills) {
    List<SuggestionSkill> unaggregated = skills.stream().flatMap(s -> s.getSuggestions().stream())
      .collect(Collectors.toList());
    List<SuggestionSkill> aggregated = new ArrayList<>();

    for (SuggestionSkill s : unaggregated) {
      Optional<SuggestionSkill> present = aggregated.stream()
        .filter(a -> a.getName().equals(s.getName()))
        .findAny();
      if (present.isPresent()) {
        present.get().incrementCount(s.getCount());
      } else {
        aggregated.add(s);
      }
    }
    return aggregated;
  }

  public List<KnownSkill> getSuggestionSkills(List<String> references, int count) {
    if (count < 1) {
      throw new IllegalArgumentException("count must be a positive integer");
    }

    List<SuggestionSkill> suggestions;
    if (CollectionUtils.isEmpty(references)) {
      suggestions = aggregateSuggestions(skillRepository.findAllExcludeHidden());
    } else {
      Set<KnownSkill> sanitizedReferenceskills = getSkillsByStemsExcludeHidden(references);
      List<String> sanitizedReferenceNames = sanitizedReferenceskills.stream()
        .map(KnownSkill::getName)
        .collect(Collectors.toList());
      suggestions = aggregateSuggestions(sanitizedReferenceskills).stream()
        .filter(s -> !sanitizedReferenceNames.contains(s.getName()))
        .collect(Collectors.toList());
    }

    return suggestions.stream()
      .sorted(Comparator.comparingInt(SuggestionSkill::getCount).reversed())
      .limit(count)
      .map(s -> skillRepository.findByName(s.getName()))
      .filter(s -> !s.isHidden())
      .collect(Collectors.toList());
  }

  public void registerSkillSearch(Collection<KnownSkill> searchedSkills) throws IllegalArgumentException {

    if (searchedSkills.size() < 2) {
      logger.debug("Searched for less than two skills, cannot update mutual suggestions");
      return;
    }

    for (KnownSkill s : searchedSkills) {
      List<KnownSkill> ts = searchedSkills.stream().filter(x -> !x.equals(s)).collect(Collectors.toList());
      for (KnownSkill t : ts) {
        s.incrementSuggestion(t.getName());
      }
    }

    try {
      skillRepository.saveAll(searchedSkills);
    } catch (OptimisticLockingFailureException e)  {
      logger.error("Failed to register search for {} - optimistic locking error; will ignore search",
        searchedSkills.stream().map(KnownSkill::getName).collect(Collectors.joining(", ")));
    }

    logger.info("Successfully registered search for {}",
      searchedSkills.stream().map(KnownSkill::getName).collect(Collectors.joining(", ")));
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void createSkill(String name, boolean isHidden, Set<String> subSkills)
    throws EmptyArgumentException, DuplicateSkillException {

    name = SkillUtils.sanitizeName(name);
    subSkills = subSkills.stream().map(n -> SkillUtils.sanitizeName(n)).filter(n -> !StringUtils.isEmpty(n)).collect(Collectors.toSet());

    if (StringUtils.isEmpty(name)) {
      throw new EmptyArgumentException("name is empty");
    }

    if (skillRepository.findByName(name) != null) {
      logger.debug("Failed to create skill {}: already exists", name);
      throw new DuplicateSkillException("skill already existing");
    }

    // check if subSkills are known
    if (!isValidSubSkills(subSkills)) {
      logger.debug("Failed to set subskills on skill {}: subskill not found", name);
      throw new SkillNotFoundException("cannot set subskill: not found");
    }

    try {
      skillRepository.insert(new KnownSkill(name, new ArrayList<>(), isHidden, subSkills));
      logger.info("Successfully created skill {}", name);
    } catch (DuplicateKeyException e) {
      logger.debug("Failed to create skill {}: already exists");
      throw new DuplicateSkillException("skill already existing");
    }

  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void updateSkill(String name, String newName, Boolean hidden, Set<String> subSkills)
    throws IllegalArgumentException, DuplicateSkillException, SkillNotFoundException {

    name = SkillUtils.sanitizeName(name);
    newName = SkillUtils.sanitizeName(newName);
    subSkills = subSkills.stream().map(n -> SkillUtils.sanitizeName(n)).filter(n -> !StringUtils.isEmpty(n)).collect(Collectors.toSet());
    KnownSkill oldSkill;
    KnownSkill newSkill;

    if (StringUtils.isEmpty(name)) {
      throw new SkillNotFoundException("skill not found");
    }

    oldSkill = skillRepository.findByName(name);
    if (oldSkill == null) {
      logger.info("Failed to update {}: skill not found", name);
      throw new SkillNotFoundException("skill not found");
    }

    if (skillRepository.findByName(newName) != null) {
      logger.info("Failed to update skill {}: new name {} already exists", name, newName);
      throw new DuplicateSkillException("skillname already exists");
    }

    if (!isValidSubSkills(subSkills)) {
      logger.info("Failed to update skill {}: one or more subskills not found");
      throw new SkillNotFoundException("one new subskill cannot be found");
    }

    // @formatter:off
    newSkill = new KnownSkill(
      StringUtils.isEmpty(newName) ? oldSkill.getName() : newName,
      oldSkill.getSuggestions(),
      hidden == null ? oldSkill.isHidden() : hidden,
      CollectionUtils.isEmpty(subSkills) ? oldSkill.getSubSkillNames() : subSkills
    );
    // @formatter:on

    if (newSkill.equals(oldSkill)) {
      logger.info("Failed to update skill {}: new values contain no changes");
      return;
    }

    skillRepository.delete(oldSkill);
    skillRepository.insert(newSkill);

    if (!StringUtils.isEmpty(newName)) {
      updateInSubskills(oldSkill, newSkill);
      updateInSuggestions(oldSkill, newSkill);
      updateInPersons(oldSkill, newSkill);
    } else if (hidden != null) {
      updateInPersons(oldSkill, newSkill);
    }
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void updateInSuggestions(KnownSkill oldSkill, KnownSkill newSkill) {
    List<KnownSkill> containingSkills = skillRepository.findBySuggestion(oldSkill.getName());
    containingSkills.forEach(s -> s.renameSuggestion(oldSkill.getName(), newSkill.getName()));
    skillRepository.saveAll(containingSkills);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void updateInSubskills(KnownSkill oldSkill, KnownSkill newSkill) {
    List<KnownSkill> containingSkills = skillRepository.findBySubskillName(oldSkill.getName());
    containingSkills.forEach(s -> s.renameSubSkill(oldSkill.getName(), newSkill.getName()));
    skillRepository.saveAll(containingSkills);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void updateInPersons(KnownSkill oldSkill, KnownSkill newSkill) {
    logger.debug("updating Skill {} in persons", oldSkill.getName());
    List<Person> persons = personRepository.findBySkill(oldSkill.getName());

    persons.forEach(p -> {
      PersonalSkill oldPersonalSkill = p.getSkill(oldSkill.getName());
      p.addUpdateSkill(newSkill.getName(), oldPersonalSkill.getSkillLevel(), oldPersonalSkill.getWillLevel(), newSkill.isHidden(), oldPersonalSkill.isMentor());
    });
    personRepository.saveAll(persons);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void deleteSkill(String name, String migrateTo) throws IllegalArgumentException {
    KnownSkill deleteSkill = skillRepository.findByName(name);
    if (deleteSkill == null) {
      logger.debug("Failed to delete skill {}: not found", name);
      throw new SkillNotFoundException("skill not found");
    }

    if (!StringUtils.isEmpty(migrateTo)) {
      KnownSkill migrateSkill = skillRepository.findByName(migrateTo);
      migratePersonalSkills(deleteSkill, migrateSkill);
    }

    // delete from persons
    for (Person person : personRepository.findBySkill(name)) {
      person.removeSkill(name);
      personRepository.save(person);
    }

    // delete from known skills
    skillRepository.delete(skillRepository.findByName(name));

    // delete in suggestion
    for (KnownSkill knownSkill : skillRepository.findBySuggestion(name)) {
      knownSkill.deleteSuggestion(name);
      skillRepository.save(knownSkill);
    }

    logger.info("Successfully deleted skill {}", name);
  }

  private void migratePersonalSkills(KnownSkill from, KnownSkill to) throws IllegalArgumentException {
    if (from == null || to == null) {
      logger.info("Failed to migrate {} to {}: not found", from, to);
      throw new SkillNotFoundException("Failed to migrate personal skills");
    } else if (from.getName().equals(to.getName())) {
      logger.info("Failed to migrate {} to {}: source and target equal");
      throw new IllegalArgumentException("Source and target may not be equal");
    }

    List<Person> migrateables = personRepository.findBySkill(from.getName()).stream()
      .filter(user -> !user.hasSkill(to.getName()))
      .collect(Collectors.toList());

    migrateables.forEach(user -> {
      PersonalSkill oldSkill = user.getSkill(from.getName());
      user.addUpdateSkill(to.getName(), oldSkill.getSkillLevel(), oldSkill.getWillLevel(), to.isHidden(), oldSkill.isMentor());
      user.removeSkill(from.getName());
    });

    personRepository.saveAll(migrateables);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public boolean isHidden(String skillName) {
    KnownSkill skill = skillRepository.findByName(skillName);
    if (skill == null) {
      throw new SkillNotFoundException("skill not found");
    }
    return skill.isHidden();
  }

  private boolean isValidSubSkills(Collection<String> subSkills) {
    return CollectionUtils.isEmpty(subSkills) || subSkills.size() == skillRepository.findByNameIn(subSkills).size();
  }

}
