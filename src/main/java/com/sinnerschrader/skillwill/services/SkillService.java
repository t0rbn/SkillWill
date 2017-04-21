package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.domain.skills.KnownSkillSuggestionComparator;
import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;
import com.sinnerschrader.skillwill.domain.skills.SkillStemUtils;
import com.sinnerschrader.skillwill.domain.skills.SuggestionSkill;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

  private List<KnownSkill> getAllSkills() {
    return skillRepository.findAll();
  }

  private List<KnownSkill> getAutocompleteSkills(String input) {
    List<KnownSkill> skills = skillRepository.findByNameStemLike(SkillStemUtils.nameToStem(input));
    skills.sort(new KnownSkillSuggestionComparator(input));
    logger.debug("Successfully got {} autocompletions for : {}", skills.size(), input);
    return skills;
  }

  public List<KnownSkill> getSkills(String search) {
    return StringUtils.isEmpty(search) ? getAllSkills() : getAutocompleteSkills(search);
  }

  public KnownSkill getSkillByName(String name) {
    return skillRepository.findByName(name);
  }

  public List<KnownSkill> getSkillsByStems(List<String> stems, boolean checkFindAll) throws SkillNotFoundException {
      List<KnownSkill> skills = stems.stream()
          .map(s -> skillRepository.findByNameStem(SkillStemUtils.nameToStem(s)))
          .collect(Collectors.toList());

      if (checkFindAll && skills.contains(null)) {
        throw new SkillNotFoundException("cannot find all skills for search query " + stems.toString());
      }

      return skills.stream().filter(s -> s != null).collect(Collectors.toList());
  }

  private List<SuggestionSkill> aggregateSuggestions(List<KnownSkill> skills) {
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
      suggestions = aggregateSuggestions(skillRepository.findAll());
    } else {
      List<KnownSkill> sanitizedReferenceskills = getSkillsByStems(references, false);
      List<String> sanitizedReferenceNames = sanitizedReferenceskills.stream()
          .map(KnownSkill::getName)
          .collect(Collectors.toList());
      suggestions = aggregateSuggestions(sanitizedReferenceskills).stream()
          .filter(s ->  !sanitizedReferenceNames.contains(s.getName()))
          .collect(Collectors.toList());
    }

    return suggestions.stream()
        .sorted(Comparator.comparingInt(SuggestionSkill::getCount).reversed())
        .limit(count)
        .map(s -> skillRepository.findByName(s.getName()))
        .collect(Collectors.toList());
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void registerSkillSearch(List<KnownSkill> searchedSkills) throws IllegalArgumentException {

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

    skillRepository.save(searchedSkills);
    logger.info("Successfully registered search for {}",
        searchedSkills.stream().map(KnownSkill::getName).collect(Collectors.toList()));
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void createSkill(String name, String iconDescriptor)
      throws EmptyArgumentException, DuplicateSkillException {

    if (StringUtils.isEmpty(name)) {
      logger.debug("Failed to create skill {}: name is empty", name);
      throw new EmptyArgumentException("name is empty");
    }

    if (skillRepository.findByName(name) != null) {
      logger.debug("Failed to create skill {}: already exists", name);
      throw new DuplicateSkillException("skill already existing");
    }

    try {
      skillRepository.insert(new KnownSkill(name, iconDescriptor));
      logger.info("Successfully created skill {}", name);
    } catch (DuplicateKeyException e) {
      logger.debug("Failed to create skill {}: already exists");
      throw new DuplicateSkillException("skill already existing");
    }

  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void updateSkill(String name, String newName, String iconDescriptor)
      throws IllegalArgumentException, DuplicateSkillException {

    if (skillRepository.findByName(name) == null) {
      logger.debug("Failed to rename skill {}: not found", name);
      throw new SkillNotFoundException("skill not found");
    }

    if (!name.equals(newName) && skillRepository.findByName(newName) != null) {
      logger.debug("Failed to rename skill {}: new name {} already exists", name, newName);
      throw new DuplicateSkillException("skill already exists");
    }

    // icondescriptor may be empty string!
    if (StringUtils.isEmpty(newName) || iconDescriptor == null) {
      logger.debug("Failed to rename skill {}: name and iconDescriptor must not be empty", name);
      throw new EmptyArgumentException("new name must not be empty");
    }

    // update in skills Repo
    try {
      KnownSkill skill = skillRepository.findByName(name);
      KnownSkill newSkill = new KnownSkill(newName, iconDescriptor, skill.getSuggestions());
      skillRepository.delete(skill);
      skillRepository.insert(newSkill);
    } catch (DuplicateSkillException e) {
      throw new DuplicateSkillException("skill already exists");
    }

    // rename in suggestions and persons
    if (!name.equals(newName)) {
      renameSkillinPersons(name, newName);
      renameSkillInSuggestions(name, newName);
    }

    logger.info("Successfully renamed skill {} to {}", name, newName);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void renameSkillInSuggestions(String name, String newName) {
    logger.debug("Renaming Skill {} to {} in skill suggestions", name, newName);
    for (KnownSkill knownSkill : skillRepository.findBySuggestion(name)) {
      knownSkill.renameSuggestion(name, newName);
      skillRepository.save(knownSkill);
    }
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void renameSkillinPersons(String name, String newName) {
    logger.debug("Renaming Skill {} to {} in persons", name, newName);
    for (Person person : personRepository.findBySkill(name)) {
      PersonalSkill oldSkill = person.getSkill(name);
      person.addUpdateSkill(newName, oldSkill.getSkillLevel(), oldSkill.getWillLevel());
      person.deleteSkill(name);
      personRepository.save(person);
    }
  }


  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void deleteSkill(String name) {
    if (skillRepository.findByName(name) == null) {
      logger.debug("Failed to delete skill {}: not found", name);
      throw new SkillNotFoundException("skill not found");
    }

    // delete from known skills
    skillRepository.delete(skillRepository.findByName(name));

    // delete in suggestion
    for (KnownSkill knownSkill : skillRepository.findBySuggestion(name)) {
      knownSkill.deleteSuggestion(name);
      skillRepository.save(knownSkill);
    }

    // delete from persons
    for (Person person : personRepository.findBySkill(name)) {
      person.deleteSkill(name);
      personRepository.save(person);
    }

    logger.info("Successfully deleted skill {}", name);
  }

}
