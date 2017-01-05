package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.testinfrastructure.EmbeddedLdap;
import com.unboundid.ldap.sdk.LDAPException;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for SkillController
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SkillControllerTest {

	@Autowired
	private SkillController skillController;

	@Autowired
	private SkillsRepository skillRepo;

	@Autowired
	private EmbeddedLdap embeddedLdap;

	@Before
	public void setUp() throws LDAPException, IOException {
		embeddedLdap.reset();
		skillRepo.deleteAll();

		KnownSkill javaSkill = new KnownSkill("Java");
		javaSkill.incrementSuggestion("COBOL");
		skillRepo.insert(javaSkill);

		KnownSkill cobolSkill = new KnownSkill("COBOL");
		cobolSkill.incrementSuggestion("Java");
		skillRepo.insert(cobolSkill);
	}

	@Test
	public void testGetSkillsValidQuery() throws JSONException {
		ResponseEntity<String> res = skillController.getSkills("COB");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONArray resJSON = new JSONArray(res.getBody());
		assertTrue(resJSON.length() == 1);
		assertEquals("COBOL", resJSON.getString(0));
	}

	@Test
	public void testGetSkillsEmptyQuery() throws JSONException {
		ResponseEntity<String> res = skillController.getSkills("");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONArray resJSON = new JSONArray(res.getBody());
		assertTrue(resJSON.length() == 2);
		assertEquals("Java", resJSON.get(0));
		assertEquals("COBOL", resJSON.get(1));
	}

	@Test
	public void testGetSkillsUnknownSkill() throws JSONException {
		ResponseEntity<String> res = skillController.getSkills("glibberish");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONArray resJSON = new JSONArray(res.getBody());
		assertTrue(resJSON.length() == 0);
	}

	@Test
	public void testGetNextValid() {
		ResponseEntity<String> res = skillController.getNext("Java");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertEquals("COBOL", res.getBody());
	}


	@Test
	public void testgetNextDoubleSearch() {
		ResponseEntity<String> res = skillController.getNext("Java, COBOL");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertEquals("", res.getBody());
	}

	@Test
	public void testGetNextEmptySearch() {
		ResponseEntity<String> res = skillController.getNext("");
		assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testGetNextUnknownSearch() {
		ResponseEntity<String> res = skillController.getNext("IAmUnknown");
		assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testAddSkillValid() throws JSONException {
		assertTrue(skillController.addSkill("foo").getStatusCode() == HttpStatus.OK);
		assertEquals("foo", new JSONArray(skillController.getSkills("fo").getBody()).getString(0));
	}

	@Test
	public void testAddSkillEmptyName() {
		assertTrue(skillController.addSkill("").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testAddSkillDuplicateName() {
		assertTrue(skillController.addSkill("Java").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testDeleteValid() {
		assertTrue(skillController.deleteSkill("Java").getStatusCode() == HttpStatus.OK);
		assertEquals("", skillController.getNext("COBOL").getBody());
	}

	@Test
	public void testDeleteEmpty() {
		assertTrue(skillController.deleteSkill("").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testDeleteUnknown() {
		assertTrue(skillController.deleteSkill("foo").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testEditSkillValid() {
		assertTrue(skillController.editSkill("COBOL", "foo").getStatusCode() == HttpStatus.OK);
		assertEquals("foo", skillController.getNext("Java").getBody());
	}

	@Test
	public void testEditSkillEmptyName() {
		assertTrue(skillController.editSkill("", "foo").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testEditSkilEmptyName() {
		assertTrue(skillController.editSkill("Java", "").getStatusCode() == HttpStatus.BAD_REQUEST);
		assertEquals("Java", skillController.getNext("COBOL").getBody());
	}

	@Test
	public void testEditSkillUnknown() {
		assertTrue(skillController.editSkill("foo", "bar").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testEditSkillToExisting() {
		assertTrue(skillController.editSkill("Java", "COBOL").getStatusCode() == HttpStatus.BAD_REQUEST);
		assertEquals("COBOL", skillController.getNext("Java").getBody());
		assertEquals("Java", skillController.getNext("COBOL").getBody());
	}

}