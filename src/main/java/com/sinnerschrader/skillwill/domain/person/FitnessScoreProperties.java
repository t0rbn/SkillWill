package com.sinnerschrader.skillwill.domain.person;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Wrapper to load weights for fitness Scores
 */
@Component
public class FitnessScoreProperties {

  @Value("${weightAverageSkills}")
  private double weightAverageSkills;

  @Value("${weightAverageWills}")
  private double weightAverageWills;


  @Value("${weightSpecializationSkills}")
  private double weightSpecializationSkills;

  @Value("${weightSpecializationWills}")
  private double weightSpecializationWills;

  @Value("${maxLevelValue}")
  private int maxLevelValue;

  public double getWeightAverageSkills() {
    return weightAverageSkills;
  }

  public double getWeightAverageWills() {
    return weightAverageWills;
  }

  public double getWeightSpecializationSkills() {
    return weightSpecializationSkills;
  }

  public double getWeightSpecializationWills() {
    return weightSpecializationWills;
  }

  public int getMaxLevelValue() {
    return maxLevelValue;
  }

}
