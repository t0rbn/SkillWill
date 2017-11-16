package com.sinnerschrader.skillwill.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import org.json.HTTP;
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
    ResponseEntity<String> res = skillController.getSkills("COB", true, -1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONArray resJSON = new JSONArray(res.getBody());
    assertEquals(1, resJSON.length());
    assertEquals("COBOL", resJSON.getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetSkillsEmptyQuery() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("", true, -1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONArray resJSON = new JSONArray(res.getBody());
    assertEquals(2, resJSON.length());
    assertEquals("Java", resJSON.getJSONObject(0).getString("name"));
    assertEquals("COBOL", resJSON.getJSONObject(1).getString("name"));
  }

  @Test
  public void testGetSkillsUnknownSkill() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("glibberish", true, -1);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONArray resJSON = new JSONArray(res.getBody());
    assertEquals(0, resJSON.length());
  }

  @Test
  public void testGetSkillsHidden() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("hidden", true, -1);
    assertEquals(0, new JSONArray(res.getBody()).length());
    res = skillController.getSkills("hidden", false, -1);
    assertEquals(1, new JSONArray(res.getBody()).length());
    assertEquals("hidden skill", new JSONArray(res.getBody()).getJSONObject(0).getString("name"));
  }

  @Test
  public void testGetSkillsCountNegative() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("", true, -1);
    assertEquals(2, new JSONArray(res.getBody()).length());
  }

  @Test
  public void testGetSkillsCountZero() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("", true, 0);
    assertEquals(0, new JSONArray(res.getBody()).length());
  }

  @Test
  public void testGetSkillsCountPositive() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("", true, 1);
    assertEquals(1, new JSONArray(res.getBody()).length());
  }

  @Test
  public void testGetSkillsCountHigherThanFound() throws JSONException {
    ResponseEntity<String> res = skillController.getSkills("", true, 42);
    assertEquals(2, new JSONArray(res.getBody()).length());
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
    assertEquals("foo", new JSONArray(skillController.getSkills("fo", true, -1).getBody()).getJSONObject(0).getString("name"));
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
    assertEquals(HttpStatus.OK, skillController.deleteSkill("Java", "adminsessionkey", null).getStatusCode());
    assertEquals(0, new JSONArray(skillController.getNext("COBOL", 1).getBody()).length());
  }

  @Test
  public void testDeleteEmpty() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("", "adminsessionkey", null).getStatusCode());
  }

  @Test
  public void testDeleteUnknown() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("foo", "adminsessionkey", null).getStatusCode());
  }

  @Test
  public void testDeleteUnprivilegedRole() {
    assertEquals(HttpStatus.FORBIDDEN, skillController.deleteSkill("foo", "usersessionkey", null).getStatusCode());
  }

  @Test
  public void testDeleteMigrateTargetNotFound() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("Java", "adminsessionkey", "Ramalamadingdong").getStatusCode());
    assertNotNull(skillRepo.findByName("Java"));
  }

  @Test
  public void testDeleteMigrateToSource() {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.deleteSkill("Java", "adminsessionkey", "Java").getStatusCode());
    assertNotNull(skillRepo.findByName("Java"));
  }

  @Test
  public void testDeleteMigrate() {
    Person aUser = personRepo.findByIdIgnoreCase("aaaaaa");
    aUser.addUpdateSkill("Java", 1, 3, false, true);
    personRepo.save(aUser);

    assertEquals(HttpStatus.OK, skillController.deleteSkill("Java", "adminsessionkey", "COBOL").getStatusCode());
    assertNull(skillRepo.findByName("Java"));

    aUser = personRepo.findByIdIgnoreCase("aaaaaa");
    assertEquals(1, aUser.getSkill("COBOL").getSkillLevel());
    assertEquals(3, aUser.getSkill("COBOL").getWillLevel());
    assertTrue(aUser.getSkill("COBOL").isMentor());
    assertFalse(aUser.getSkill("COBOL").isHidden());
    assertNull(aUser.getSkill("Java"));
  }

  @Test
  public void testDeleteMigrateAlreadyHavingTarget() {
    Person aUser = personRepo.findByIdIgnoreCase("aaaaaa");
    aUser.addUpdateSkill("Java", 1, 3, false, true);
    aUser.addUpdateSkill("COBOL", 2, 0, false, false);
    personRepo.save(aUser);

    assertEquals(HttpStatus.OK, skillController.deleteSkill("Java", "adminsessionkey", "COBOL").getStatusCode());
    assertNull(skillRepo.findByName("Java"));

    aUser = personRepo.findByIdIgnoreCase("aaaaaa");
    assertEquals(2, aUser.getSkill("COBOL").getSkillLevel());
    assertEquals(0, aUser.getSkill("COBOL").getWillLevel());
    assertFalse(aUser.getSkill("COBOL").isMentor());
    assertFalse(aUser.getSkill("COBOL").isHidden());
    assertNull(aUser.getSkill("Java"));
  }

  @Test
  public void testDeleteMigrateNoSkill() {
    Person aUser = personRepo.findByIdIgnoreCase("aaaaaa");
    aUser.addUpdateSkill("Java", 1, 3, false, true);
    personRepo.save(aUser);

    assertEquals(HttpStatus.OK, skillController.deleteSkill("COBOL", "adminsessionkey", "Java").getStatusCode());
    assertNull(skillRepo.findByName("COBOL"));

    aUser = personRepo.findByIdIgnoreCase("aaaaaa");
    assertEquals(1, aUser.getSkill("Java").getSkillLevel());
    assertEquals(3, aUser.getSkill("Java").getWillLevel());
    assertTrue(aUser.getSkill("Java").isMentor());
    assertFalse(aUser.getSkill("Java").isHidden());
    assertNull(aUser.getSkill("COBOL"));
  }

  @Test
  public void testEditValid() {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "foobar", null, null, "adminsessionkey").getStatusCode());
    assertNull(skillRepo.findByName("Java"));
    assertNotNull(skillRepo.findByName("foobar"));
  }

  @Test
  public void testEditOldUnknown() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill("foobar", "barfoo", null, null, "adminsessionkey").getStatusCode());
    assertNull(skillRepo.findByName("barfoo"));
    assertNull(skillRepo.findByName("foobar"));
  }

  @Test
  public void testEditOldEmpty() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill("", "barfoo", null, null, "adminsessionkey").getStatusCode());
    assertNull(skillRepo.findByName(""));
    assertNull(skillRepo.findByName("foobar"));
  }

  @Test
  public void testEditOldNull() {
    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill(null, "barfoo", null, null, "adminsessionkey").getStatusCode());
    assertNull(skillRepo.findByName("barfoo"));
  }

  @Test
  public void testEditNewExisting() {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.updateSkill("Java", "COBOL", null, null, "adminsessionkey").getStatusCode());
    assertNotNull(skillRepo.findByName("Java"));
  }

  @Test
  public void testEditNewEmptyString() {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "", null, null, "adminsessionkey").getStatusCode());
    assertNotNull(skillRepo.findByName("Java"));
    assertNull(skillRepo.findByName(""));
  }

  @Test
  public void testEditNewNull() {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", null, null, null, "adminsessionkey").getStatusCode());
    assertNotNull(skillRepo.findByName("Java"));
  }

  @Test
  public void testEditOldHasSpace() {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java ", "Foobar", null, null, "adminsessionkey").getStatusCode());
    assertNotNull(skillRepo.findByName("Foobar"));
  }

  @Test
  public void testEditNewHasSpaceValid() {
    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "Foobar ", null, null, "adminsessionkey").getStatusCode());
    assertNotNull(skillRepo.findByName("Foobar"));
  }

  @Test
  public void testEditNewHasSpaceExisting() {
    assertEquals(HttpStatus.BAD_REQUEST, skillController.updateSkill("Java", " COBOL", null, null, "adminsessionkey").getStatusCode());
    assertNotNull(skillRepo.findByName("Java"));
  }

  @Test
  public void testEditKeepSubskills() {
   skillController.updateSkill("Java", "foobar", null, null, "adminsessionkey");
   assertEquals("COBOL", skillRepo.findByName("foobar").getSuggestions().get(0).getName());
  }

  @Test
  public void testEditRenameSubskills() {
    skillController.updateSkill("Java", "foobar", null, null, "adminsessionkey");
    assertEquals("foobar", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
  }

  @Test
  public void testEditRenameSubskillsWithSpace() {
    skillController.updateSkill("Java", "foobar ", null, null, "adminsessionkey");
    assertEquals("foobar", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
  }

  @Test
  public void testEditKeepPersonalSkillsWithNull() {
    Person p = personRepo.findByIdIgnoreCase("aaaaaa");
    p.addUpdateSkill("Java", 3, 3, false, true);
    personRepo.save(p);

    skillController.updateSkill("Java", null, null, null, "adminsessionkey");
    assertEquals("aaaaaa", personRepo.findBySkill("Java").get(0).getId());
  }

  @Test
  public void testEditKeepPersonalSkillsWithEmptyl() {
    Person p = personRepo.findByIdIgnoreCase("aaaaaa");
    p.addUpdateSkill("Java", 3, 3, false, true);
    personRepo.save(p);

    skillController.updateSkill("Java", "", null, null, "adminsessionkey");
    assertEquals("aaaaaa", personRepo.findBySkill("Java").get(0).getId());
  }

  @Test
  public void testEditRenamePersonalSkills() {
    Person p = personRepo.findByIdIgnoreCase("aaaaaa");
    p.addUpdateSkill("Java", 3, 3, false, true);
    personRepo.save(p);

    skillController.updateSkill("Java", "Foobar", null, null, "adminsessionkey");
    assertEquals("aaaaaa", personRepo.findBySkill("Foobar").get(0).getId());
  }

  @Test
  public void testEditRenamePersonalSkillsWithSpace() {
    Person p = personRepo.findByIdIgnoreCase("aaaaaa");
    p.addUpdateSkill("Java", 3, 3, false, true);
    personRepo.save(p);

    skillController.updateSkill("Java", "Foobar ", null, null, "adminsessionkey");
    assertEquals("aaaaaa", personRepo.findBySkill("Foobar").get(0).getId());
  }

  @Test
  public void testEditKeepPersonalSkillsVisibility() {
    Person p = personRepo.findByIdIgnoreCase("aaaaaa");
    p.addUpdateSkill("Java", 3, 3, false, true);
    personRepo.save(p);

    skillController.updateSkill("Java", "", null, null, "adminsessionkey");
    assertFalse(personRepo.findBySkill("Java").get(0).getSkill("Java").isHidden());
  }

  @Test
  public void testEditPersonalSkillsHide() {
    Person p = personRepo.findByIdIgnoreCase("aaaaaa");
    p.addUpdateSkill("Java", 3, 3, false, true);
    personRepo.save(p);

    skillController.updateSkill("Java", "", true, null, "adminsessionkey");
    assertTrue(personRepo.findBySkill("Java").get(0).getSkill("Java").isHidden());
  }

  @Test
  public void testEditKeepSuggestions() {
    skillController.updateSkill("Java", "", null, null, "adminsessionkey");
    assertEquals(1, skillRepo.findByName("COBOL").getSuggestions().size());
    assertEquals("Java", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
  }

  @Test
  public void testEditRenameSuggestions() {
    skillController.updateSkill("Java", "Foobar", null, null, "adminsessionkey");
    assertEquals(1, skillRepo.findByName("COBOL").getSuggestions().size());
    assertEquals("Foobar", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
  }

  @Test
  public void testEditRenameSuggestionsWithSpace() {
    skillController.updateSkill("Java", "   Foobar ", null, null, "adminsessionkey");
    assertEquals(1, skillRepo.findByName("COBOL").getSuggestions().size());
    assertEquals("Foobar", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
  }

  @Test
  public void testEditSkillUnprivileged() {
    assertEquals(HttpStatus.FORBIDDEN, skillController.updateSkill("Java", null, false, "Rama Lama, Ding Ding, Dong", "useressionkey").getStatusCode());
  }

}
