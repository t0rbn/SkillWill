package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.skills.KnownSkill;
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

import static org.junit.Assert.assertTrue;

/**
 * Integration test for SkillController
 *
 * @author torree
 *
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
		assertTrue(resJSON.getString(0).equals("COBOL"));
	}

	@Test
	public void testGetSkillsEmptyQuery() throws JSONException {
		ResponseEntity<String> res = skillController.getSkills("");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONArray resJSON = new JSONArray(res.getBody());
		assertTrue(resJSON.length() == 2);
		assertTrue(resJSON.get(0).equals("Java"));
		assertTrue(resJSON.get(1).equals("COBOL"));
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
		assertTrue(res.getBody().equals("COBOL"));
	}


	@Test
	public void testgetNextDoubleSearch() {
		ResponseEntity<String> res = skillController.getNext("Java, COBOL");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertTrue(res.getBody().equals(""));
	}

	@Test
	public void testGetNextEmptySearch() {
		ResponseEntity<String> res = skillController.getNext("");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertTrue(res.getBody().equals(""));
	}

	@Test
	public void testGetNextUnknownSearch() {
		ResponseEntity<String> res = skillController.getNext("I am Unknown");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertTrue(res.getBody().equals(""));
	}

	@Test
	public void testAddSkillValid() throws JSONException {
		assertTrue(skillController.addSkill("foo").getStatusCode() == HttpStatus.OK);
		assertTrue(new JSONArray(skillController.getSkills("fo").getBody()).getString(0).equals("foo"));
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
		assertTrue(skillController.getNext("COBOL").getBody().equals(""));
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
		assertTrue(skillController.getNext("Java").getBody().equals("foo"));
	}

	@Test
	public void testEditSkillEmptyName() {
		assertTrue(skillController.editSkill("", "foo").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testEditSkilEmptyName() {
		assertTrue(skillController.editSkill("Java", "").getStatusCode() == HttpStatus.BAD_REQUEST);
		assertTrue(skillController.getNext("COBOL").getBody().equals("Java"));
	}

	@Test
	public void testEditSkillUnknown() {
		assertTrue(skillController.editSkill("foo", "bar").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testEditSkillToExisting() {
		assertTrue(skillController.editSkill("Java", "COBOL").getStatusCode() == HttpStatus.BAD_REQUEST);
		assertTrue(skillController.getNext("Java").getBody().equals("COBOL"));
		assertTrue(skillController.getNext("COBOL").getBody().equals("Java"));
	}

}