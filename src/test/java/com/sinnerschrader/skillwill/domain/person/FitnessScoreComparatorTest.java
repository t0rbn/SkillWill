package com.sinnerschrader.skillwill.domain.person;

import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for FitnessScoreComparator
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FitnessScoreComparatorTest {

	private Person pGood;
	private Person pBad;
	private FitnessScoreComparator comparator;

	@Autowired
	private FitnessScoreProperties fitnessScoreProperties;

	@Before
	public void init() {
		pGood = new Person("foobar");
		pGood.addUpdateSkill("skill 1", 3, 2);
		pGood.addUpdateSkill("skill 2", 2, 3);

		pBad = new Person("barfoo");
		pBad.addUpdateSkill("skill 1", 0, 1);
		pBad.addUpdateSkill("skill 2", 1, 0);

		List<String> searchItems = new ArrayList<String>();
		searchItems.add("skill 1");
		searchItems.add("skill 2");

		comparator = new FitnessScoreComparator(searchItems, fitnessScoreProperties);
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
