package com.sinnerschrader.skillwill.person;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinnerschrader.skillwill.skills.PersonalSkill;

public class FitnessScore {

	private double value;

	public FitnessScore(Person person, List<String> searchItems) {
		this.value =
				0.5 * (getAverageSearchedSkills(person, searchItems) / 3) +
				0.25 * (getAverageSearchedWills(person, searchItems) / 3) + 
				0.125 * getSpecializationSkills(person, searchItems) + 
				0.125 * getSpecializationWills(person, searchItems);
	}

	public double getValue() {
		return this.value;
	}

	private static List<PersonalSkill> commonSkills(Person person, List<String> searchItems) {
		return person.getSkills().stream()
				.filter(skill -> searchItems.contains(skill.getName()))
				.collect(Collectors.toList());
	}

	private static double getAverageSearchedSkills(Person person, List<String> searchItems) {
		List <PersonalSkill> commonSkills = commonSkills(person, searchItems);
		int count = commonSkills.size();
		int sum = commonSkills.stream().mapToInt(skill -> skill.getSkillLevel().getInt()).sum();

		return sum/count;
	}

	private static double getAverageSearchedWills(Person person, List<String> searchItems) {
		List <PersonalSkill> commonSkills = commonSkills(person, searchItems);
		int count = commonSkills.size();
		int sum = commonSkills.stream().mapToInt(skill -> skill.getWillLevel().getInt()).sum();

		return sum/count;
	}

	private static double getSpecializationSkills(Person person, List<String> searchItems) {
		double searchedAverage = getAverageSearchedSkills(person, searchItems);
		double personAverage = person.getSkills().stream().mapToInt(s -> s.getSkillLevel().getInt()).sum() / person.getSkills().size();
		
		return (3 + searchedAverage - personAverage) / 6;
	}

	private static double getSpecializationWills(Person person, List<String> searchItems) {
		double searchedAverage = getAverageSearchedWills(person, searchItems);
		double personAverage = person.getSkills().stream().mapToInt(s -> s.getWillLevel().getInt()).sum() / person.getSkills().size();
		
		return (3 + searchedAverage - personAverage) / 6;
	}

	
}