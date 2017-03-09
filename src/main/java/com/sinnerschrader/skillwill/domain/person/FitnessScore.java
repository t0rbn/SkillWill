package com.sinnerschrader.skillwill.domain.person;

import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculate how well a person fits into a searched skill set.
 * The result can be on a scale from 0 (does not fit at all) to 1 (perfect match)
 *
 * @author torree
 */
public class FitnessScore {

	private final double value;
	private final FitnessScoreProperties props;

	// total score => weighted average of all factors.
	public FitnessScore(Person person, List<String> searchItems, FitnessScoreProperties props) {
		this.props = props;

		double weightedAverageSearchedSkills = props.getWeightAverageSkills() * (getAverageSearchedSkills(person, searchItems) > 0 ? getAverageSearchedSkills(person, searchItems) / 3 : 0);
		double weightedAverageSearchedWills = props.getWeightAverageWills() * (getAverageSearchedWills(person, searchItems) > 0 ? getAverageSearchedWills(person, searchItems) / 3 : 0);
		double weightedSpecializationSkills = props.getWeightSpecializationSkills() * getSpecializationSkills(person, searchItems);
		double weightedSpecializationWills = props.getWeightSpecializationWills() * getSpecializationWills(person, searchItems);

		// Round fitness to four digits -> eg. 0.4223 = 42.23% match
		this.value = Math.round((weightedAverageSearchedSkills + weightedAverageSearchedWills + weightedSpecializationSkills + weightedSpecializationWills) * 10000.0) / 10000.0;
	}

	public double getValue() {
		return this.value;
	}

	// Helper method finding the intersection of the person's and the searched skill set
	private List<PersonalSkill> searchedSkillsInPerson(Person person, List<String> searchItems) {
		return person.getSkills().stream()
				.filter(skill -> searchItems.contains(skill.getName()))
				.collect(Collectors.toList());
	}

	private List<PersonalSkill> unsearchedSkillsInPerson(Person person, List<String> searchItems) {
		return person.getSkills().stream()
				.filter(skill -> !searchItems.contains(skill.getName()))
				.collect(Collectors.toList());
	}


	// Average skill level of person regarding searched items
	private double getAverageSearchedSkills(Person person, List<String> searchItems) {
		List<PersonalSkill> relevantSkills = searchedSkillsInPerson(person, searchItems);
		int count = relevantSkills.size();
		int sum = relevantSkills.stream().mapToInt(skill -> skill.getSkillLevel()).sum();
		return count > 0 ? (double) sum / count : 0;
	}

	// Average will level of person regarding searched items
	private double getAverageSearchedWills(Person person, List<String> searchItems) {
		List<PersonalSkill> relevantSkills = searchedSkillsInPerson(person, searchItems);
		int count = relevantSkills.size();
		int sum = relevantSkills.stream().mapToInt(skill -> skill.getWillLevel()).sum();
		return count > 0 ? (double) sum / count : 0;
	}

	// Average skill level of person regarding searched items
	private double getAverageUnsearchedSkills(Person person, List<String> searchItems) {
		List<PersonalSkill> relevantSkills = unsearchedSkillsInPerson(person, searchItems);
		int count = relevantSkills.size();
		int sum = relevantSkills.stream().mapToInt(skill -> skill.getSkillLevel()).sum();
		return count > 0 ? (double) sum / count : 0;
	}

	// Average will level of person regarding searched items
	private double getAverageUnsearchedWills(Person person, List<String> searchItems) {
		List<PersonalSkill> relevantSkills = unsearchedSkillsInPerson(person, searchItems);
		int count = relevantSkills.size();
		int sum = relevantSkills.stream().mapToInt(skill -> skill.getWillLevel()).sum();
		return count > 0 ? (double) sum / count : 0;
	}

	// Specialization in skill level of person regarding searched items (0: worst; 1: best)
	private double getSpecializationSkills(Person person, List<String> searchItems) {
		double searchedAverage = getAverageSearchedSkills(person, searchItems);
		double unsearchedAverage = getAverageUnsearchedSkills(person, searchItems);
		return (this.props.getMaxLevelValue() + searchedAverage - unsearchedAverage) / (2 * this.props.getMaxLevelValue());
	}

	// Specialization in will level of person regarding searched items (0: worst; 1: best)
	private double getSpecializationWills(Person person, List<String> searchItems) {
		double searchedAverage = getAverageSearchedWills(person, searchItems);
		double unsearchedAverage = getAverageUnsearchedWills(person, searchItems);
		return (this.props.getMaxLevelValue() + searchedAverage - unsearchedAverage) / (2 * this.props.getMaxLevelValue());
	}

}