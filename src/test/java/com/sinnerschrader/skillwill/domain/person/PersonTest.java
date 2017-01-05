package com.sinnerschrader.skillwill.domain.person;

import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Partial unit tests for Person
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PersonTest {

	public Person person;

	@Before
	public void init() {
		person = new Person("foobar");
		person.addUpdateSkill("skillname", 2, 3);
	}

	@Test
	public void testAddUpdateNewSkill() {
		person.addUpdateSkill("new skill", 2, 3);
		assertEquals(2, person.getSkills().size());
	}

	@Test
	public void testAddUpdateKnownSkill() {
		person.addUpdateSkill("skillname", 0, 1);
		assertEquals(1, person.getSkills().size());
		assertEquals(0, person.getSkills().get(0).getSkillLevel());
		assertEquals(1, person.getSkills().get(0).getWillLevel());
	}

}
