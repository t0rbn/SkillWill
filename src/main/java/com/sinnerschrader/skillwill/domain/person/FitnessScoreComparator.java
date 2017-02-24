package com.sinnerschrader.skillwill.domain.person;

import java.util.Comparator;
import java.util.List;

/**
 * Compare fitness scores, so that higher ones will be
 * sorted before lower ones
 *
 * @author torree
 */
public class FitnessScoreComparator implements Comparator<Person> {

	private final List<String> searchItems;

	private final FitnessScoreProperties fitnessScoreProperties;

	public FitnessScoreComparator(List<String> searchItems, FitnessScoreProperties fitnessScoreProperties) {
		this.searchItems = searchItems;
		this.fitnessScoreProperties = fitnessScoreProperties;
	}

	@Override
	public final int compare(Person a, Person b) {
		double scoreA = new FitnessScore(a, searchItems, fitnessScoreProperties).getValue();
		double scoreB = new FitnessScore(b, searchItems, fitnessScoreProperties).getValue();
		return scoreA > scoreB ? -1 : 1;
	}

}
