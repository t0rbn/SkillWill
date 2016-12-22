package com.sinnerschrader.skillwill.skills;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class KnownSkillSuggestionComparatorTest {

	@Test
	public void testNoneStarts () {
		KnownSkill a = new KnownSkill("Wurstwasser");
		KnownSkill b = new KnownSkill("foo");

		List<KnownSkill> toSort = new ArrayList<KnownSkill>();
		toSort.add(a);
		toSort.add(b);
		toSort.sort(new KnownSkillSuggestionComparator("42"));

		assertEquals(a, toSort.get(0));
		assertEquals(b, toSort.get(1));
	}

	@Test
	public void bothStart() {
		KnownSkill a = new KnownSkill("foobar");
		KnownSkill b = new KnownSkill("foowurst");

		List<KnownSkill> toSort = new ArrayList<KnownSkill>();
		toSort.add(a);
		toSort.add(b);
		toSort.sort(new KnownSkillSuggestionComparator("foo"));

		assertEquals(a, toSort.get(0));
		assertEquals(b, toSort.get(1));
	}

	@Test
	public void oneStarts() {
		KnownSkill a = new KnownSkill("foobar");
		KnownSkill b = new KnownSkill("wurstwasser");

		List<KnownSkill> toSort = new ArrayList<KnownSkill>();
		toSort.add(a);
		toSort.add(b);
		toSort.sort(new KnownSkillSuggestionComparator("wurst"));

		assertEquals(b, toSort.get(0));
		assertEquals(a, toSort.get(1));		
	}

	@Test
	public void threeElements() {
		KnownSkill a = new KnownSkill("foobar");
		KnownSkill b = new KnownSkill("wurstwasser");
		KnownSkill c = new KnownSkill("bumsdings");

		List<KnownSkill> toSort = new ArrayList<KnownSkill>();
		toSort.add(a);
		toSort.add(b);
		toSort.add(c);
		toSort.sort(new KnownSkillSuggestionComparator("wurst"));

		assertEquals(b, toSort.get(0));
		assertEquals(a, toSort.get(1));	
		assertEquals(c, toSort.get(2));	
		
	}

}
