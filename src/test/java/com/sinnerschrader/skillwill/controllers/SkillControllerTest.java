package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.unboundid.ldap.sdk.LDAPException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
	private SkillRepository skillRepo;

	@Autowired
	private EmbeddedLdap embeddedLdap;

	@Before
	public void setUp() throws LDAPException, IOException {
		embeddedLdap.reset();
		skillRepo.deleteAll();

		KnownSkill javaSkill = new KnownSkill("Java", "java descriptor");
		javaSkill.incrementSuggestion("COBOL");
		skillRepo.insert(javaSkill);

		KnownSkill cobolSkill = new KnownSkill("COBOL", "cobol descriptor");
		cobolSkill.incrementSuggestion("Java");
		skillRepo.insert(cobolSkill);
	}

	@Test
	public void testGetSkillsValidQuery() throws JSONException {
		ResponseEntity<String> res = skillController.getSkills("COB");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONArray resJSON = new JSONArray(res.getBody());
		assertTrue(resJSON.length() == 1);
		assertEquals("COBOL", resJSON.getJSONObject(0).getString("name"));
	}

	@Test
	public void testGetSkillsEmptyQuery() throws JSONException {
		ResponseEntity<String> res = skillController.getSkills("");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONArray resJSON = new JSONArray(res.getBody());
		assertTrue(resJSON.length() == 2);
		assertEquals("Java", resJSON.getJSONObject(0).getString("name"));
		assertEquals("COBOL", resJSON.getJSONObject(1).getString("name"));
	}

	@Test
	public void testGetSkillsUnknownSkill() throws JSONException {
		ResponseEntity<String> res = skillController.getSkills("glibberish");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONArray resJSON = new JSONArray(res.getBody());
		assertTrue(resJSON.length() == 0);
	}

	@Test
	public void testGetNextValid() throws JSONException {
		ResponseEntity<String> res = skillController.getNext("Java", 1);
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertEquals("COBOL", new JSONArray(res.getBody()).getJSONObject(0).getString("name"));
	}

	@Test
	public void testGetNextCountZero() throws JSONException {
		ResponseEntity<String> res = skillController.getNext("Java", 0);
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertEquals(0, new JSONArray(res.getBody()).length());
	}

	@Test
	public void testGetNextCountNegative() {
		ResponseEntity<String> res = skillController.getNext("Java", -1);
		assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testGetNextCountMoreThanSkills() throws JSONException {
		ResponseEntity<String> res = skillController.getNext("Java", 42);
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertEquals(1, new JSONArray(res.getBody()).length());
		assertEquals("COBOL", new JSONArray(res.getBody()).getJSONObject(0).get("name"));
	}


	@Test
	public void testgetNextDoubleSearch() throws JSONException {
		ResponseEntity<String> res = skillController.getNext("Java, COBOL", 1);
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertEquals(0, new JSONArray(res.getBody()).length());
	}

	@Test
	public void testGetNextEmptySearch() {
		ResponseEntity<String> res = skillController.getNext("", 1);
		assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testGetNextUnknownSearch() {
		ResponseEntity<String> res = skillController.getNext("IAmUnknown", 1);
		assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testAddSkillValid() throws JSONException {
		assertTrue(skillController.addSkill("foo", "foo descriptor").getStatusCode() == HttpStatus.OK);
		assertEquals("foo", new JSONArray(skillController.getSkills("fo").getBody()).getJSONObject(0).getString("name"));
	}

	@Test
	public void testAddSkillEmptyName() {
		assertTrue(skillController.addSkill("", "").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testAddSkillDuplicateName() {
		assertTrue(skillController.addSkill("Java", "java descriptor").getStatusCode() == HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testDeleteValid() throws JSONException {
		assertTrue(skillController.deleteSkill("Java").getStatusCode() == HttpStatus.OK);
		assertEquals(0, new JSONArray(skillController.getNext("COBOL", 1).getBody()).length());
	}

	@Test
	public void testDeleteEmpty() {
		assertTrue(skillController.deleteSkill("").getStatusCode() == HttpStatus.NOT_FOUND);
	}

	@Test
	public void testDeleteUnknown() {
		assertTrue(skillController.deleteSkill("foo").getStatusCode() == HttpStatus.NOT_FOUND);
	}

	@Test
	public void testEditSkillValid() throws JSONException {
		assertTrue(skillController.updateSkill("COBOL", "foo", "").getStatusCode() == HttpStatus.OK);
		assertEquals("foo", new JSONArray(skillController.getNext("Java", 1).getBody()).getJSONObject(0).get("name"));
	}

	@Test
	public void testEditSkillEmptyName() {
		assertTrue(skillController.updateSkill("", "foo", "").getStatusCode() == HttpStatus.NOT_FOUND);
	}

	@Test
	public void testEditSkillEmptyNewName() throws JSONException {
		assertTrue(skillController.updateSkill("Java", "", "").getStatusCode() == HttpStatus.BAD_REQUEST);
		assertEquals("Java", new JSONArray(skillController.getNext("COBOL", 1).getBody()).getJSONObject(0).get("name"));
	}

	@Test
	public void testEditSkillUnknown() {
		assertTrue(skillController.updateSkill("foo", "bar", "bar descriptor").getStatusCode() == HttpStatus.NOT_FOUND);
	}

	@Test
	public void testEditSkillToExisting() throws JSONException {
		assertTrue(skillController.updateSkill("Java", "COBOL" ,"").getStatusCode() == HttpStatus.BAD_REQUEST);
		assertEquals("COBOL", new JSONArray(skillController.getNext("Java", 1).getBody()).getJSONObject(0).get("name"));
		assertEquals("Java", new JSONArray(skillController.getNext("COBOL", 1).getBody()).getJSONObject(0).get("name"));
	}

	@Test
	public void testEditSkillValidIconDescriptor() throws JSONException {
		skillController.updateSkill("Java", "Java", "This is a new descriptor");
		ResponseEntity<String> res = skillController.getSkill("Java");
		assertEquals("This is a new descriptor", new JSONObject(res.getBody()).getString("iconDescriptor"));
	}

	@Test
	public void testEditSkillEmptyDescriptor() throws JSONException {
		assertTrue(skillController.updateSkill("Java", "Java", "").getStatusCode() == HttpStatus.OK);
		System.out.println(skillController.updateSkill("Java", "Java", "").getStatusCode().toString());
		ResponseEntity<String> res = skillController.getSkill("Java");
		assertEquals("", new JSONObject(res.getBody()).getString("iconDescriptor"));
	}

	@Test
	public void testEditSkillUnicodeDescriptor() throws JSONException {
		assertTrue(skillController.updateSkill("Java", "Java", "đây là một biểu tượng").getStatusCode() == HttpStatus.OK);
		ResponseEntity<String> res = skillController.getSkill("Java");
		assertEquals("đây là một biểu tượng", new JSONObject(res.getBody()).getString("iconDescriptor"));
	}

	@Test
	public void testEditSkillNullDescriptor() throws JSONException {
		assertTrue(skillController.updateSkill("Java", "Java", null).getStatusCode() == HttpStatus.BAD_REQUEST);
		ResponseEntity<String> res = skillController.getSkill("Java");
		assertEquals("java descriptor", new JSONObject(res.getBody()).getString("iconDescriptor"));
	}

}