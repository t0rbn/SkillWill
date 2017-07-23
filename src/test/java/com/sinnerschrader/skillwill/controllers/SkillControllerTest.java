package com.sinnerschrader.skillwill.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.person.Role;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SessionRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.session.Session;
import com.unboundid.ldap.sdk.LDAPException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
  private PersonRepository personRepo;

  @Autowired
  private SessionRepository sessionRepo;

  @Autowired
  private EmbeddedLdap embeddedLdap;

  @Before
  public void setUp() throws LDAPException, IOException {
    embeddedLdap.reset();
    skillRepo.deleteAll();
    sessionRepo.deleteAll();
    personRepo.deleteAll();

    KnownSkill hiddenSkill = new KnownSkill("hidden skill", new ArrayList<>(), true, new HashSet<>());
    skillRepo.insert(hiddenSkill);

    KnownSkill javaSkill = new KnownSkill("Java");
    javaSkill.incrementSuggestion("COBOL");
    javaSkill.incrementSuggestion("hidden skill");
    skillRepo.insert(javaSkill);

    KnownSkill cobolSkill = new KnownSkill("COBOL");
    cobolSkill.incrementSuggestion("Java");
    skillRepo.insert(cobolSkill);


    Person userPerson = new Person("aaaaaa");
    personRepo.insert(userPerson);

    Person adminPerson = new Person("bbbbbb");
    adminPerson.setRole(Role.ADMIN);
    personRepo.insert(adminPerson);

    Session userSession = new Session("usersessionkey" ,"aaaaaa", new Date());
    userSession.renewSession(60);
    sessionRepo.insert(userSession);

    Session adminSession = new Session("adminsessionkey", "bbbbbb", new Date());
    adminSession.renewSession(60);
    sessionRepo.insert(adminSession);
  }

  @Test
  public void testGetSkillsValidQuery() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("COB", true);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONArray resJSON = new JSONArray(res.getBody());
    assertEquals(1, resJSON.length());
    assertEquals("COBOL", resJSON.getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetSkillsEmptyQuery() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("", true);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONArray resJSON = new JSONArray(res.getBody());
    assertEquals(2, resJSON.length());
    assertEquals("Java", resJSON.getJSONObject(0).getString("name"));
    assertEquals("COBOL", resJSON.getJSONObject(1).getString("name"));
  }

  @Test
  public void testGetSkillsUnknownSkill() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("glibberish", true);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONArray resJSON = new JSONArray(res.getBody());
    assertEquals(0, resJSON.length());
  }

  @Test
  public void testGetSkillsHidden() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("hidden", true);
    assertEquals(0, new JSONArray(res.getBody()).length());
    res = skillController.getSkills("hidden", false);
    assertEquals(1, new JSONArray(res.getBody()).length());
    assertEquals("hidden skill", new JSONArray(res.getBody()).getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetNextValid() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("Java", 1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("COBOL", new JSONArray(res.getBody()).getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetNextIgnoreCase() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("JaVa", 1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("COBOL", new JSONArray(res.getBody()).getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetNextStemminng() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("j#a)_V®a", 1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("COBOL", new JSONArray(res.getBody()).getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetNextCountZero() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("Java", 0);
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testGetNextCountNegative() {
    ResponseEntity<String> res = skillController.getNext("Java", -1);
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testGetNextCountMoreThanSkills() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("Java", 42);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(1, new JSONArray(res.getBody()).length());
    assertEquals("COBOL", new JSONArray(res.getBody()).getJSONObject(0).get("name"));
  }

  @Test
  public void testGetNextExcludeHidden() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("Java", 2);
    assertEquals(1, new JSONArray(res.getBody()).length());
    assertNotEquals("hidden skill", new JSONArray(res.getBody()).getString(0));
  }


  @Test
  public void testgetNextDoubleSearch() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("Java, COBOL", 1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(0, new JSONArray(res.getBody()).length());
  }

  @Test
  public void testGetNextEmptySearch() {
    ResponseEntity<String> res = skillController.getNext("", 1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  public void testGetNextUnknownSearch() throws JSONException {
    ResponseEntity<String> res = skillController.getNext("IAmUnknown", 1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(0, new JSONArray(res.getBody()).length());
  }

  @Test
  public void testAddSkillValid() throws JSONException {
    assertEquals(HttpStatus.OK, skillController.addSkill("foo", false, "", "adminsessionkey").getStatusCode());
    assertEquals("foo", new JSONArray(skillController.getSkills("fo", true).getBody()).getJSONObject(0).getString("name"));
  }

  @Test
  public void testAddSkillEmptyName() {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.addSkill("", false, "", "adminsessionkey").getStatusCode());
  }

  @Test
  public void testAddSkillDuplicateName() {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.addSkill("Java", false, "", "adminsessionkey").getStatusCode());
  }

  @Test
  public void testAddSkillWithSubskills() {
    assertEquals(HttpStatus.OK, skillController.addSkill("New Skill", false, "Java, COBOL", "adminsessionkey").getStatusCode());
    assertEquals(HttpStatus.OK, skillController.getSkill("New Skill").getStatusCode());
  }

  @Test
  public void testAddSkillWithUnknownSubskill() {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.addSkill("New Skill", false, "Java, COBOL, Wurstwasser", "adminsessionkey").getStatusCode());
    assertEquals(HttpStatus.NOT_FOUND, skillController.getSkill("New Skill").getStatusCode());
  }

  @Test
  public void testAddSkillWithHiddenSubskill() {
    assertEquals(HttpStatus.OK, skillController.addSkill("New Skill", false, "Java, hidden skill", "adminsessionkey").getStatusCode());
  }

  @Test
  public  void testAddSkillUnprivilegedRole() {
    assertEquals(HttpStatus.FORBIDDEN, skillController.addSkill("New Skill", false, "Java, hidden skill", "usersessionkey").getStatusCode());
  }

  @Test
  public void testDeleteValid() throws JSONException {
    assertEquals(HttpStatus.OK, skillController.deleteSkill("Java", "adminsessionkey").getStatusCode());
    assertEquals(0, new JSONArray(skillController.getNext("COBOL", 1).getBody()).length());
  }

  @Test
  public void testDeleteEmpty() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("", "adminsessionkey").getStatusCode());
  }

  @Test
  public void testDeleteUnknown() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("foo", "adminsessionkey").getStatusCode());
  }

  @Test
  public void testDeleteUnprivilegedRole() {
    assertEquals(HttpStatus.FORBIDDEN, skillController.deleteSkill("foo", "usersessionkey").getStatusCode());
  }

  @Test
  public void testEditSkillValid() throws JSONException {
    assertEquals(HttpStatus.OK, skillController.updateSkill("COBOL", "foo", false, "Java", "adminsessionkey").getStatusCode());
    assertEquals("foo", new JSONArray(skillController.getNext("Java", 1).getBody()).getJSONObject(0).get("name"));
  }

  @Test
  public void testEditSkillEmptyName() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill("", "", false, "COBOL", "adminsessionkey").getStatusCode());
  }

  @Test
  public void testEditSkillNullName() throws JSONException {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", null, false, "COBOL", "adminsessionkey").getStatusCode());
    assertEquals("Java", new JSONArray(skillController.getNext("COBOL", 1).getBody()).getJSONObject(0).get("name"));
  }

  @Test
  public void testEditSkillUnknown() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill("foo", "bar", false, "COBOL", "adminsessionkey").getStatusCode());
  }

  @Test
  public void testEditSkillToExisting() throws JSONException {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.updateSkill("Java", "COBOL", false, "COBOL", "adminsessionkey").getStatusCode());
    assertEquals("COBOL", new JSONArray(skillController.getNext("Java", 1).getBody()).getJSONObject(0).get("name"));
    assertEquals("Java", new JSONArray(skillController.getNext("COBOL", 1).getBody()).getJSONObject(0).get("name"));
  }

  @Test
  public void testEditSkillValidsubSkill() throws JSONException {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "Java", false, "COBOL", "adminsessionkey").getStatusCode());
    ResponseEntity<String> res = skillController.getSkill("Java");
    JSONArray resSubSkillsJson = new JSONObject(res.getBody()).getJSONArray("subskills");
    assertEquals(1, resSubSkillsJson.length());
    assertEquals("COBOL", resSubSkillsJson.getString(0));
  }

  @Test
  public void testEditSkillEmptySubSkill() throws JSONException {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "Java", false, "", "adminsessionkey").getStatusCode());
    assertEquals(0, new JSONObject(skillController.getSkill("Java").getBody()).getJSONArray("subskills").length());
  }

  @Test
  public void testEditSkillNullSubSkill() throws JSONException {
    JSONArray beforeSubskills = new JSONObject(skillController.getSkill("Java").getBody()).getJSONArray("subskills");
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", null, null, null, "adminsessionkey").getStatusCode());
    JSONArray afterSubSkills = new JSONObject(skillController.getSkill("Java").getBody()).getJSONArray("subskills");
    assertEquals(beforeSubskills, afterSubSkills);
  }

  @Test
  public void testEditSkillUnknownSubskill() {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.updateSkill("Java", null, false, "Rama Lama, Ding Ding, Dong", "adminsessionkey").getStatusCode());
  }

  @Test
  public void testEditSkillunprivileged() {
    assertEquals(HttpStatus.FORBIDDEN, skillController.updateSkill("Java", null, false, "Rama Lama, Ding Ding, Dong", "useressionkey").getStatusCode());
  }

}