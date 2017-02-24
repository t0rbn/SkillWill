package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.domain.skills.KnownSkillSuggestionComparator;
import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;
import com.sinnerschrader.skillwill.domain.skills.SuggestionSkill;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Services handling skills management (create, rename, suggest, delete, ...)
 *
 * @author torree
 */
@Service
@EnableRetry
public class SkillService {

	private static Logger logger = LoggerFactory.getLogger(SkillService.class);

	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private PersonRepository personRepository;

	private List<KnownSkill> getAllSkills() {
		return skillRepository.findAll();
	}

	public boolean skillExists(String name) {
		return skillRepository.findByName(name) != null;
	}

	private List<KnownSkill> getAutocompleteSkills(String input) {
		List<KnownSkill> skills = skillRepository.findByNameLike(input);
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

	public List<String> getSuggestionNames(List<String> references, int count) {
		List<SuggestionSkill> suggestions = new ArrayList<>();
		SuggestionSkill currentmax = null;

		if (count < 0) {
			throw new IllegalArgumentException("count must be a positive integer");
		}

		for (String name : references) {
			KnownSkill skill = skillRepository.findByName(name);
			if (skill == null) {
				logger.debug("Failed to find suggestions for {}: skill not found", name);
				throw new SkillNotFoundException("skill does not exist");
			}

			for (SuggestionSkill suggestion : skill.getSuggestions()) {
				Optional<SuggestionSkill> existing = suggestions.stream().filter(s -> s.getName().equals(suggestion.getName())).findFirst();
				if (existing.isPresent() && !references.contains(existing.get().getName())) {
					existing.get().incrementCount(suggestion.getCount());
				} else if (!references.contains(suggestion.getName())) {
					suggestions.add(suggestion);
				}
			}

		}

		List<String> suggestionNames = suggestions.stream()
				.sorted(Comparator.comparingInt(SuggestionSkill::getCount).reversed())
				.limit(count)
				.map(s -> s.getName())
				.collect(Collectors.toList());

		logger.debug("Successfully got {} suggestions for {}", suggestionNames.size(), references);
		return suggestionNames;
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	public void registerSkillSearch(List<String> searchitems) throws IllegalArgumentException {
		for (String skillName : searchitems) {
			KnownSkill current = skillRepository.findByName(skillName);
			if (current == null) {
				logger.debug("Failed to register search for {}: not found", skillName);
				throw new IllegalArgumentException("skill not found");
			}
			searchitems.stream().filter(s -> !s.equals(current.getName())).forEach(s -> current.incrementSuggestion(s));
			skillRepository.save(current);
		}

		if (!searchitems.isEmpty()) {
			logger.info("Successfully registered search for {}", searchitems);
		}
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	public void createSkill(String name, String iconDescriptor) throws EmptyArgumentException, DuplicateSkillException {
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
	public void updateSkill(String name, String newName, String iconDescriptor) throws IllegalArgumentException, DuplicateSkillException {
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
