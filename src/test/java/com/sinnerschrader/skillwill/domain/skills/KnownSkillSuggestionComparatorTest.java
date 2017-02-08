package com.sinnerschrader.skillwill.domain.skills;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for KnownSkikllSuggestionComparator
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KnownSkillSuggestionComparatorTest {

	@Test
	public void testNoneStarts() {
		KnownSkill a = new KnownSkill("Wurstwasser", "icon descriptor");
		KnownSkill b = new KnownSkill("foo", "icon descriptor");

		List<KnownSkill> toSort = new ArrayList<>();
		toSort.add(a);
		toSort.add(b);
		toSort.sort(new KnownSkillSuggestionComparator("42"));

		assertEquals(a, toSort.get(0));
		assertEquals(b, toSort.get(1));
	}

	@Test
	public void bothStart() {
		KnownSkill a = new KnownSkill("foobar", "icon descriptor");
		KnownSkill b = new KnownSkill("foowurst", "icon descriptor");

		List<KnownSkill> toSort = new ArrayList<>();
		toSort.add(a);
		toSort.add(b);
		toSort.sort(new KnownSkillSuggestionComparator("foo"));

		assertEquals(a, toSort.get(0));
		assertEquals(b, toSort.get(1));
	}

	@Test
	public void oneStarts() {
		KnownSkill a = new KnownSkill("foobar", "icon descriptor");
		KnownSkill b = new KnownSkill("wurstwasser", "icon descriptor");

		List<KnownSkill> toSort = new ArrayList<>();
		toSort.add(a);
		toSort.add(b);
		toSort.sort(new KnownSkillSuggestionComparator("wurst"));

		assertEquals(b, toSort.get(0));
		assertEquals(a, toSort.get(1));
	}

	@Test
	public void threeElements() {
		KnownSkill a = new KnownSkill("foobar", "icon descriptor");
		KnownSkill b = new KnownSkill("wurstwasser", "icon descriptor");
		KnownSkill c = new KnownSkill("bumsdings", "icon descriptor");

		List<KnownSkill> toSort = new ArrayList<>();
		toSort.add(a);
		toSort.add(b);
		toSort.add(c);
		toSort.sort(new KnownSkillSuggestionComparator("wurst"));

		assertEquals(b, toSort.get(0));
		assertEquals(a, toSort.get(1));
		assertEquals(c, toSort.get(2));

	}

}
