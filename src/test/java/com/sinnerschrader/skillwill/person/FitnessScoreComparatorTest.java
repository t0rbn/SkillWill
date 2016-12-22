package com.sinnerschrader.skillwill.person;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sinnerschrader.skillwill.skills.PersonalSkill;

public class FitnessScoreComparatorTest {

	private Person pGood;
	private Person pBad;
	private FitnessScoreComparator comparator;

	@Before
	public void init() {
		pGood = new Person("foobar");
		pGood.addUpdateSkill(new PersonalSkill("skill 1", 3, 2));
		pGood.addUpdateSkill(new PersonalSkill("skill 2", 2, 3));
		
		pBad = new Person("barfoo");
		pBad.addUpdateSkill(new PersonalSkill("skill 1", 0, 1));
		pBad.addUpdateSkill(new PersonalSkill("skill 2", 1, 0));

		List<String> searchItems = new ArrayList<String>();
		searchItems.add("skill 1");
		searchItems.add("skill 2");
		
		comparator = new FitnessScoreComparator(searchItems);
	}

	@Test
	public void testKeepOrder() {
		List<Person> toSort = new ArrayList<Person>();
		toSort.add(pGood);
		toSort.add(pBad);

		// Double input order
		assertTrue(toSort.get(0) == pGood);
		assertTrue(toSort.get(1) == pBad);

		toSort.sort(comparator);

		assertTrue(toSort.get(0) == pGood);
		assertTrue(toSort.get(1) == pBad);
	}

	@Test
	public void testReorder() {
		List<Person> toSort = new ArrayList<Person>();
		toSort.add(pBad);
		toSort.add(pGood);

		// Check input order
		assertTrue(toSort.get(0) == pBad);
		assertTrue(toSort.get(1) == pGood);

		toSort.sort(comparator);

		assertTrue(toSort.get(0) == pGood);
		assertTrue(toSort.get(1) == pBad);
	}

}
