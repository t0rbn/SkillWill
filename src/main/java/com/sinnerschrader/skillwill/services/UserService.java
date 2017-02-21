package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.person.FitnessScoreComparator;
import com.sinnerschrader.skillwill.domain.person.FitnessScoreProperties;
import com.sinnerschrader.skillwill.domain.person.JaccardFilter;
import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.LevelOutOfRangeException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.exceptions.UserNotFoundException;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service handling user management
 *
 * @author torree
 */
@Service
@EnableRetry
public class UserService {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private LdapService ldapService;

	@Autowired
	private SkillService skillService;

	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private FitnessScoreProperties fitnessScoreProperties;

	@Value("${maxLevelValue}")
	private int maxLevelValue;

	public List<Person> getUsers(List<String> skills, String location) throws IllegalArgumentException {
		List<Person> candidates;

		if (skills == null || skills.isEmpty()) {
			candidates = personRepository.findAll();
		} else {
			for (String s : skills) {
				if (!skillService.skillExists(s)) {
					logger.debug("Failed to search for skill {}: not found", s);
					throw new SkillNotFoundException("skill " + s + " not found");
				}
 			}

			candidates = personRepository.findBySkills(skills);
			candidates.sort(new FitnessScoreComparator(skills, fitnessScoreProperties));
		}

		// sync needed to search for location
		candidates = ldapService.syncUsers(candidates, false);
		candidates = filterByLocation(candidates, location);

		logger.debug("Successfully found {} users for search skill={} location={}", candidates.size(), skills, location);
		return candidates;
	}

	private List<Person> filterByLocation(List<Person> unfiltered, String location) {
		if (location == null || StringUtils.isEmpty(location)) {
			return unfiltered;
		}
		return unfiltered.stream()
				.filter(p -> p.getLdapDetails().getLocation().equals(location))
				.collect(Collectors.toList());
	}

	public Person getUser(String id) {
		Person p = personRepository.findById(id);

		if (p == null) {
			logger.debug("Failed to find user {}: not found", id);
			throw new UserNotFoundException("user not found");
		}

		if (p.getLdapDetails() == null) {
			ldapService.syncUser(p);
		}

		logger.debug("Successfully found user {}", id);
		return p;
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void updateSkills(String username, String skillName, int skillLevel, int willLevel) throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(skillName)) {
			logger.debug("Failed to modify skills: username or skillName empty");
			throw new EmptyArgumentException("arguments must not be empty or null");
		}

		Person person = personRepository.findById(username);

		if (person == null) {
			logger.debug("Failed to modify {}'s skills: user not found", username);
			throw new UserNotFoundException("user not found");
		}

		if (skillRepository.findByName(skillName) == null) {
			logger.debug("Failed to modify {}'s skill {}: skill not found", username, skillName);
			throw new SkillNotFoundException("skill not found");
		}

		if (!isValidLevel(skillLevel) || !isValidLevel(willLevel)) {
			logger.debug("Failed to modify {}'s skill {}: new value out of range", username, skillName);
			throw new LevelOutOfRangeException("skill/will level out of range");
		}

		person.addUpdateSkill(skillName, skillLevel, willLevel);
		personRepository.save(person);

		logger.info("Successfully updated {}'s skill {}", username, skillName);
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void removeSkills(String username, String skillName) throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(skillName)) {
			logger.debug("Failed to modify skills: username or skillName empty");
			throw new EmptyArgumentException("arguments must not be empty or null");
		}

		Person person = personRepository.findById(username);

		if (person == null) {
			logger.debug("Failed to remove {}'s skills: user not found", username);
			throw new UserNotFoundException("user not found");
		}

		if (skillRepository.findByName(skillName) == null) {
			logger.debug("Failed to remove {}'s skill {}: skill not found", username, skillName);
			throw new SkillNotFoundException("skill not found");
		}

		person.removeSkill(skillName);
		personRepository.save(person);
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void updateDetails(String username, String comment) throws EmptyArgumentException, UserNotFoundException {
		if (StringUtils.isEmpty(username)) {
			logger.debug("Failed to update comment: username or empty");
			throw new EmptyArgumentException("username must not be empty");
		}

		Person person = personRepository.findById(username);

		if (person == null) {
			logger.debug("Failed to modify {}'s comment: user not found", username);
			throw new UserNotFoundException("user not found");
		}

		// if parameter is empty string, comment will default to null
		person.setComment(comment);

		// Add more details to update here

		personRepository.save(person);
		logger.info("Successfully updated {}'s comment", username);
	}

	private boolean isValidLevel(int level) {
		return 0 <= level && level <= maxLevelValue;
	}

	public List<Person> getSimilar(String username, int count) throws UserNotFoundException {
		if (count < 0) {
			throw new IllegalArgumentException("count must be a positive integer");
		}

		List<Person> toSearch = personRepository.findAll();
		Optional<Person> person = toSearch.stream().filter(p -> p.getId().equals(username)).findAny();

		if (!person.isPresent()) {
			logger.debug("Failed to get users similar to {}: user not found", username);
			throw new UserNotFoundException("user not found");
		}

		toSearch.remove(person.get());
		return ldapService.syncUsers(new JaccardFilter(person.get()).getFrom(toSearch, count), false);
	}

}
