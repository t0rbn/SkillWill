package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.skills.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.FitnessScoreProperties;
import com.sinnerschrader.skillwill.domain.user.UserSimilarityUtils;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.IllegalLevelConfigurationException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.exceptions.UserNotFoundException;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service handling user management
 *
 * @author torree
 */
@Service
@EnableRetry
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  private final SkillRepository skillRepository;

  private final FitnessScoreProperties fitnessScoreProperties;

  @Value("${maxLevelValue}")
  private int maxLevelValue;

  @Autowired
  public UserService(UserRepository userRepository, SkillRepository skillRepository, FitnessScoreProperties fitnessScoreProperties) {
    this.userRepository = userRepository;
    this.skillRepository = skillRepository;
    this.fitnessScoreProperties = fitnessScoreProperties;
  }

  public List<User> getUsers(SkillSearchResult skillSearch)
      throws IllegalArgumentException {

    if (skillSearch.isInputEmpty()) {
      return userRepository.findAll();
    } else {
      var skillNames = skillSearch.mappedSkills().stream().map(Skill::getName).collect(Collectors.toList());
      return userRepository.findBySkills(skillNames).stream()
          .peek(p -> p.setFitnessScore(skillSearch.mappedSkills(), fitnessScoreProperties))
          .sorted(Comparator.comparingDouble(User::getFitnessScoreValue).reversed())
          .collect(Collectors.toList());
    }
  }

  public User getUser(String email) {
    var user = userRepository.findByEmailIgnoreCase(email);

    if (user == null) {
      logger.debug("Failed to find user {}: not found", email);
      throw new UserNotFoundException("user not found");
    }

    logger.debug("Successfully found user {}", email);
    return user;
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void updateSkills(String email, String skillName, int skillLevel, int willLevel, boolean mentor)
      throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {

    if (StringUtils.isEmpty(email) || StringUtils.isEmpty(skillName)) {
      logger.debug("Failed to modify skills: username or skillName empty");
      throw new EmptyArgumentException("arguments must not be empty or null");
    }

    var user = userRepository.findByEmailIgnoreCase(email);
    if (user == null) {
      logger.debug("Failed to add/modify {}'s skills: user not found", email);
      throw new UserNotFoundException("user not found");
    }

    if (!isValidLevelConfiguration(skillLevel, willLevel)) {
      logger.debug("Failed to add/modify {}'s skill {}: illegal levels {}/{}", email, skillName,
          skillLevel, willLevel);
      throw new IllegalLevelConfigurationException("Invalid Skill-/WillLevel Configuration");
    }

    user.addUpdateSkill(skillName, skillLevel, willLevel, mentor);
    userRepository.save(user);

    logger.info("Successfully updated {}'s skill {}", email, skillName);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void removeSkills(String email, String skillName)
      throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {

    if (StringUtils.isEmpty(email) || StringUtils.isEmpty(skillName)) {
      logger.debug("Failed to modify skills: username or skillName empty");
      throw new EmptyArgumentException("arguments must not be empty or null");
    }

    var user = userRepository.findByEmailIgnoreCase(email);
    if (user == null) {
      logger.debug("Failed to remove {}'s skills: user not found", email);
      throw new UserNotFoundException("user not found");
    }

    if (skillRepository.findByName(skillName) == null) {
      logger.debug("Failed to remove {}'s skill {}: skill not found", email, skillName);
      throw new SkillNotFoundException("skill not found");
    }

    user.removeSkill(skillName);
    userRepository.save(user);
  }

  private boolean isValidLevelConfiguration(int skillLevel, int willLevel) {
    // Both levels must be between 0 and maxLevel
    // at least one level must be 1 or above (see [SKILLWILL-30])
    final boolean isValidSkillLevel = 0 <= skillLevel && skillLevel <= maxLevelValue;
    final boolean isValidWillLevel = 0 <= willLevel && willLevel <= maxLevelValue;
    final boolean isOneGreaterZero = skillLevel > 0 || willLevel > 0;
    return isValidSkillLevel && isValidWillLevel && isOneGreaterZero;
  }

  public List<User> getSimilar(String email, Integer count) throws UserNotFoundException {
    var toSearch = userRepository.findAll();
    var user = toSearch.stream().filter(p -> p.getEmail().equals(email)).findAny();

    if (!user.isPresent()) {
      logger.debug("Failed to get users similar to {}: user not found", email);
      throw new UserNotFoundException("user not found");
    }
    return UserSimilarityUtils.findSimilar(user.get(), toSearch, count);
  }

}
