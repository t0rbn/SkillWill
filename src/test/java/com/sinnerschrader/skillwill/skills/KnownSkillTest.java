package com.sinnerschrader.skillwill.skills;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Partial unit tests for KnownSkill
 *
 * @author torree
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KnownSkillTest {

	private KnownSkill skill;

	@Before
	public void setUp() {
		skill = new KnownSkill("Java");
		skill.incrementSuggestion("COBOL");
	}

	@Test
	public void testAddSuggestion() {
		skill.incrementSuggestion("PHP");
		assertEquals("PHP", skill.getSuggestions().get(1).getName());
	}

	@Test
	public void testRenameSuggestion() {
		skill.renameSuggestion("COBOL", "PHP");
		assertEquals("PHP", skill.getSuggestions().get(0).getName());
	}

	@Test
	public void testIncrementSuggestion() {
		skill.incrementSuggestion("COBOL");
		assertEquals(2, skill.getSuggestions().get(0).getCount());
	}

	@Test
	public void testDeleteSuggestion() {
		skill.deleteSuggestion("COBOL");
		assertTrue(skill.getSuggestions().isEmpty());
	}

}
