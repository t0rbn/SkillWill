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
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
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
import java.util.List;
import java.util.Optional;

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
	private SkillsRepository skillsRepository;

	@Autowired
	private PersonRepository personRepository;

	private List<KnownSkill> getAllSkills() {
		return skillsRepository.findAll();
	}

	public boolean skillExists(String name) {
		return skillsRepository.findByName(name) != null;
	}

	private List<KnownSkill> getAutocompleteSkills(String input) {
		List<KnownSkill> skills = skillsRepository.findByNameLike(input);
		skills.sort(new KnownSkillSuggestionComparator(input));
		logger.debug("Successfully got {} autocompletions for : {}", skills.size(), input);
		return skills;
	}

	public List<KnownSkill> getSkills(String search) {
		return StringUtils.isEmpty(search) ? getAllSkills() : getAutocompleteSkills(search);
	}

	public String getSuggestedSkillName(List<String> enteredNames) {
		List<SuggestionSkill> allSuggestions = new ArrayList<>();
		SuggestionSkill currentmax = null;

		for (String name : enteredNames) {
			KnownSkill skill = skillsRepository.findByName(name);
			if (skill == null) {
				logger.debug("Failed to find suggestions for {}: skill not found", name);
				throw new SkillNotFoundException("skill does not exist");
			}

			for (SuggestionSkill suggestion : skill.getSuggestions()) {
				Optional<SuggestionSkill> existing = allSuggestions.stream().filter(s -> s.getName().equals(suggestion.getName())).findFirst();
				if (existing.isPresent() && !enteredNames.contains(existing.get().getName())) {
					existing.get().incrementCount(suggestion.getCount());
				} else if (!enteredNames.contains(suggestion.getName())) {
					allSuggestions.add(suggestion);
				}
			}

		}

		for (SuggestionSkill s : allSuggestions) {
			if (currentmax == null || s.getCount() > currentmax.getCount()) {
				currentmax = s;
			}
		}

		logger.debug("Successfully got next suggestions for {}: {}", enteredNames, currentmax != null ? currentmax.getName() : "no suggestion");
		return currentmax == null ? "" : currentmax.getName();
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void registerSkillSearch(List<String> searchitems) throws IllegalArgumentException {
		for (String skillName : searchitems) {
			KnownSkill current = skillsRepository.findByName(skillName);
			if (current == null) {
				logger.debug("Failed to register search for {}: not found", skillName);
				throw new IllegalArgumentException("skill not found");
			}
			searchitems.stream().filter(s -> !s.equals(current.getName())).forEach(s -> current.incrementSuggestion(s));
			skillsRepository.save(current);
		}
		logger.info("Successfully registered search for {}", searchitems);
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void createSkill(String name) throws EmptyArgumentException, DuplicateSkillException {
		if (StringUtils.isEmpty(name)) {
			logger.debug("Failed to create skill {}: name is empty", name);
			throw new EmptyArgumentException("name is empty");
		}

		if (skillsRepository.findByName(name) != null) {
			logger.debug("Failed to create skill {}: already exists", name);
			throw new DuplicateSkillException("skill already existing");
		}

		try {
			skillsRepository.insert(new KnownSkill(name));
			logger.info("Successfully created skill {}", name);
		} catch (DuplicateKeyException e) {
			logger.debug("Failed to create skill {}: already exists");
			throw new DuplicateSkillException("skill already existing");
		}

	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void renameSkill(String name, String newName) throws IllegalArgumentException, DuplicateSkillException {
		if (skillsRepository.findByName(name) == null) {
			logger.debug("Failed to rename skill {}: not found", name);
			throw new SkillNotFoundException("skill not found");
		}

		if (skillsRepository.findByName(newName) != null) {
			logger.debug("Failed to rename skill {}: new name {} already exists", name, newName);
			throw new DuplicateSkillException("skill already exists");
		}

		if (StringUtils.isEmpty(newName)) {
			logger.debug("Failed to rename skill {}: new name must not be empty", name);
			throw new EmptyArgumentException("new name must not be empty");
		}

		// Rename in skills Repo
		try {
			KnownSkill skill = skillsRepository.findByName(name);
			KnownSkill newSkill = new KnownSkill(newName, skill.getSuggestions());
			skillsRepository.delete(skill);
			skillsRepository.insert(newSkill);
		} catch (DuplicateSkillException e) {
			throw new DuplicateSkillException("skill already exists");
		}

		// rename in suggestion
		for (KnownSkill knownSkill : skillsRepository.findBySuggestion(name)) {
			knownSkill.renameSuggestion(name, newName);
			skillsRepository.save(knownSkill);
		}

		// rename in persons
		for (Person person : personRepository.findBySkill(name)) {
			PersonalSkill oldSkill = person.getSkill(name);
			person.addUpdateSkill(newName, oldSkill.getSkillLevel(), oldSkill.getWillLevel());
			person.deleteSkill(name);
			personRepository.save(person);
		}

		logger.info("Successfully renamed skill {} to {}", name, newName);
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void deleteSkill(String name) {
		if (skillsRepository.findByName(name) == null) {
			logger.debug("Failed to delete skill {}: not found", name);
			throw new SkillNotFoundException("skill not found");
		}

		// delete from known skills
		skillsRepository.delete(skillsRepository.findByName(name));

		// delete in suggestion
		for (KnownSkill knownSkill : skillsRepository.findBySuggestion(name)) {
			knownSkill.deleteSuggestion(name);
			skillsRepository.save(knownSkill);
		}

		// delete from persons
		for (Person person : personRepository.findBySkill(name)) {
			person.deleteSkill(name);
			personRepository.save(person);
		}

		logger.info("Successfully deleted skill {}", name);
	}

}
