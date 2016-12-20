package com.sinnerschrader.skillwill.person;

import java.util.Comparator;
import java.util.List;

/**
 * 
 * Compare fitness scores, so that higher ones will be
 * sorted before lower ones
 *
 * @author torree
 *
 */
public class FitnessScoreComparator implements Comparator<Person>{

	List<String> searchItems;

	public FitnessScoreComparator(List<String> searchItems) {
		this.searchItems = searchItems;
	}

	@Override
	public int compare(Person a, Person b) {
		double scoreA = (new FitnessScore(a, searchItems)).getValue();
		double scoreB = (new FitnessScore(b, searchItems)).getValue();
		return scoreA < scoreB ? -1 : 1;
	}

}
