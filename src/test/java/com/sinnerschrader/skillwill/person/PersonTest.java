package com.sinnerschrader.skillwill.person;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sinnerschrader.skillwill.skills.PersonalSkill;

public class PersonTest {

	public Person person;

	@Before
	public void init() { 
		person = new Person("foobar");
		person.addUpdateSkill(new PersonalSkill("skillname", 2, 3));
	}

	@Test
	public void testAddUpdateNewSkill() {
		person.addUpdateSkill(new PersonalSkill("new skill", 2, 3));
		assertEquals(2, person.getSkills().size());
	}
	
	public void testAddUpdateKnownSkill() {
		person.addUpdateSkill(new PersonalSkill("skillname", 0, 1));
		assertEquals(1, person.getSkills().size());
		assertEquals(0, person.getSkills().get(0).getSkillLevel().getInt());
		assertEquals(1, person.getSkills().get(0).getWillLevel().getInt());
	}

}
