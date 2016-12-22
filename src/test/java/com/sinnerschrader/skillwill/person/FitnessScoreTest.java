package com.sinnerschrader.skillwill.person;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sinnerschrader.skillwill.skills.PersonalSkill;

public class FitnessScoreTest {

	@Test
	public void onlySearchedTest() {
		Person person = new Person("foobar");
		person.addUpdateSkill(new PersonalSkill("Java", 3, 3));
		person.addUpdateSkill(new PersonalSkill("AEM", 3, 3));

		List<String> searchItems = new ArrayList<String>();
		searchItems.add("Java");
		searchItems.add("AEM");
		assertTrue(1.0 == new FitnessScore(person, searchItems).getValue());
	}

	@Test
	public void noSearchedTest() {
		Person person = new Person("foobar");
		person.addUpdateSkill(new PersonalSkill("Java", 3, 3));
		person.addUpdateSkill(new PersonalSkill("AEM", 3, 3));

		List<String> searchItems = new ArrayList<String>();
		searchItems.add("Ruby");
		assertTrue(0.0 == new FitnessScore(person, searchItems).getValue());
	}

	@Test
	public void maximalScoreText() {
		Person person = new Person("foobar");
		person.addUpdateSkill(new PersonalSkill("Java", 3, 3));
		person.addUpdateSkill(new PersonalSkill("AEM", 3, 3));
		person.addUpdateSkill(new PersonalSkill("Foo", 0, 0));
		person.addUpdateSkill(new PersonalSkill("Bar", 0, 0));

		List<String> searchItems = new ArrayList<String>();
		searchItems.add("Java");
		searchItems.add("AEM");
		assertTrue(1.0 == new FitnessScore(person, searchItems).getValue());
	}

	@Test
	public void minimalScoreText() {
		Person person = new Person("foobar");
		person.addUpdateSkill(new PersonalSkill("Java", 0, 0));
		person.addUpdateSkill(new PersonalSkill("AEM", 0, 0));
		person.addUpdateSkill(new PersonalSkill("Foo", 3, 3));
		person.addUpdateSkill(new PersonalSkill("Bar", 3, 3));

		List<String> searchItems = new ArrayList<String>();
		searchItems.add("Java");
		searchItems.add("AEM");
		assertTrue(0.0 == new FitnessScore(person, searchItems).getValue());
	}

	@Test
	public void realisticTest() {
		Person person = new Person("foobar");
		person.addUpdateSkill(new PersonalSkill("Java", 2, 3));
		person.addUpdateSkill(new PersonalSkill("AEM", 2, 2));
		person.addUpdateSkill(new PersonalSkill("Foo", 1, 3));
		person.addUpdateSkill(new PersonalSkill("Bar", 1, 0));

		List<String> searchItems = new ArrayList<String>();
		searchItems.add("Java");
		searchItems.add("AEM");
		searchItems.add("Bar");
		assertTrue((double)19/36 == new FitnessScore(person, searchItems).getValue());
	}

}
