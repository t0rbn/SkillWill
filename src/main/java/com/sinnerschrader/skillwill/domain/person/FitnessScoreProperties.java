package com.sinnerschrader.skillwill.domain.person;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Wrapper to load weights for fitness Scores
 */
@Component
public class FitnessScoreProperties {


	public double weightAverageSkills;
	public double weightAverageWills;
	public double weightSpecializationSkills;
	public double weightSpecializationWills;
	
	@Value("${weightAverageSkills}")
	public void setWeightAverageSkills(String propString) {
		weightAverageSkills = Double.valueOf(propString);
	}

	@Value("${weightAverageWills}")
	public void setWeightAverageWills(String propString) {
		weightAverageWills= Double.valueOf(propString);
	}

	@Value("${weightSpecializationSkills}")
	public void setWeightSpecializationSkills(String propString) {
		weightSpecializationSkills= Double.valueOf(propString);
	}

	@Value("${weightSpecializationWills}")
	public void setWeightSpecializationWills(String propString) {
		weightSpecializationWills = Double.valueOf(propString);
	}
	
}
