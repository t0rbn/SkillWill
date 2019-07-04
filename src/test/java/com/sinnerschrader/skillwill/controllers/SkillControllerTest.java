//package com.sinnerschrader.skillwill.controllers;
//
//import com.sinnerschrader.skillwill.domain.skills.Skill;
//import com.sinnerschrader.skillwill.domain.user.User;
//import com.sinnerschrader.skillwill.repositories.SessionRepository;
//import com.sinnerschrader.skillwill.repositories.SkillRepository;
//import com.sinnerschrader.skillwill.repositories.UserRepository;
//import com.sinnerschrader.skillwill.session.Session;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.Objects;
//
//import static org.junit.Assert.*;
//
///**
// * Integration test for SkillController
// *
// * @author torree
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
//public class SkillControllerTest {
//
//  @Autowired
//  private SkillController skillController;
//
//  @Autowired
//  private SkillRepository skillRepo;
//
//  @Autowired
//  private UserRepository userRepo;
//
//  @Autowired
//  private SessionRepository sessionRepo;
//
//  @Before
//  public void setUp() {
//    skillRepo.deleteAll();
//    sessionRepo.deleteAll();
//    userRepo.deleteAll();
//
//    var hiddenSkill = new Skill("hidden skill");
//    skillRepo.insert(hiddenSkill);
//
//    var javaSkill = new Skill("Java");
//    javaSkill.incrementSuggestion("COBOL");
//    javaSkill.incrementSuggestion("hidden skill");
//    skillRepo.insert(javaSkill);
//
//    var cobolSkill = new Skill("COBOL");
//    cobolSkill.incrementSuggestion("Java");
//    skillRepo.insert(cobolSkill);
//
//    userRepo.insert(new User("aaaaaa"));
//    userRepo.insert(new User("bbbbbb"));
//
//    sessionRepo.insert( new Session("YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar"));
//    sessionRepo.insert(new Session("YmJiLmJiYkBleGFtcGxlLmNvbQ==|foo|bar"));
//  }
//
//  @Test
//  public void testGetSkillsValidQuery() throws JSONException {
//    var response = skillController.getSkills("COB", -1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    var responseJSON = new JSONArray(response.getBody());
//    assertEquals(1, responseJSON.length());
//    assertEquals("COBOL", responseJSON.getJSONObject(0).getString("name"));
//  }
//
//  @Test
//  public void testGetSkillsEmptyQuery() throws JSONException {
//    var response = skillController.getSkills("", -1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    var responseJson = new JSONArray(response.getBody());
//    assertEquals(2, responseJson.length());
//    assertEquals("Java", responseJson.getJSONObject(0).getString("name"));
//    assertEquals("COBOL", responseJson.getJSONObject(1).getString("name"));
//  }
//
//  @Test
//  public void testGetSkillsUnknownSkill() {
//    var response = skillController.getSkills("glibberish", -1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    var responseJson = new JSONArray(response.getBody());
//    assertEquals(0, responseJson.length());
//  }
//
//  @Test
//  public void testGetSkillsCountNegative() {
//    var response = skillController.getSkills("", -1);
//    assertEquals(2, new JSONArray(response.getBody()).length());
//  }
//
//  @Test
//  public void testGetSkillsCountZero() {
//    var response = skillController.getSkills("", 0);
//    assertEquals(2, new JSONArray(response.getBody()).length());
//  }
//
//  @Test
//  public void testGetSkillsCountPositive() {
//    var response = skillController.getSkills("", 1);
//    assertEquals(1, new JSONArray(response.getBody()).length());
//  }
//
//  @Test
//  public void testGetSkillsCountHigherThanFound() {
//    var response = skillController.getSkills("", 42);
//    assertEquals(2, new JSONArray(response.getBody()).length());
//  }
//
//  @Test
//  public void testGetNextValid() throws JSONException {
//    var response = skillController.getNext("Java", 1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals("COBOL", new JSONArray(Objects.requireNonNull(response.getBody())).getJSONObject(0).getString("name"));
//  }
//
//  @Test
//  public void testGetNextIgnoreCase() throws JSONException {
//    var response = skillController.getNext("JaVa", 1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals("COBOL", new JSONArray(Objects.requireNonNull(response.getBody())).getJSONObject(0).getString("name"));
//  }
//
//  @Test
//  public void testGetNextStemminng() throws JSONException {
//    var response = skillController.getNext("j#a)_V®a", 1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals("COBOL", new JSONArray(Objects.requireNonNull(response.getBody())).getJSONObject(0).getString("name"));
//  }
//
//  @Test
//  public void testGetNextCountZero() {
//    var response = skillController.getNext("Java", 0);
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }
//
//  @Test
//  public void testGetNextCountNegative() {
//    var response = skillController.getNext("Java", -1);
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }
//
//  @Test
//  public void testGetNextCountMoreThanSkills() throws JSONException {
//    var response = skillController.getNext("Java", 42);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(1, new JSONArray(Objects.requireNonNull(response.getBody())).length());
//    assertEquals("COBOL", new JSONArray(response.getBody()).getJSONObject(0).get("name"));
//  }
//
//  @Test
//  public void testgetNextDoubleSearch() throws JSONException {
//    var response = skillController.getNext("Java, COBOL", 1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(0, new JSONArray(Objects.requireNonNull(response.getBody())).length());
//  }
//
//  @Test
//  public void testGetNextEmptySearch() {
//    var response = skillController.getNext("", 1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//  }
//
//  @Test
//  public void testGetNextUnknownSearch() throws JSONException {
//    var response = skillController.getNext("IAmUnknown", 1);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(0, new JSONArray(Objects.requireNonNull(response.getBody())).length());
//  }
//
//  @Test
//  public void testAddSkillValid() throws JSONException {
//    assertEquals(HttpStatus.OK, skillController.addSkill("foo").getStatusCode());
//    assertEquals("foo", new JSONArray(skillController.getSkills("fo", -1).getBody()).getJSONObject(0).getString("name"));
//  }
//
//  @Test
//  public void testAddSkillDuplicateName() {
//    assertEquals(HttpStatus.BAD_REQUEST, skillController.addSkill("Java").getStatusCode());
//  }
//
//  @Test
//  public void testDeleteValid() throws JSONException {
//    assertEquals(HttpStatus.OK, skillController.deleteSkill("Java", null).getStatusCode());
//    assertEquals(0, new JSONArray(Objects.requireNonNull(skillController.getNext("COBOL", 1).getBody())).length());
//  }
//
//  @Test
//  public void testDeleteEmpty() {
//    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("", null).getStatusCode());
//  }
//
//  @Test
//  public void testDeleteUnknown() {
//    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("foo", null).getStatusCode());
//  }
//
//
//  @Test
//  public void testDeleteMigrateTargetNotFound() {
//    assertEquals(HttpStatus.NOT_FOUND, skillController.deleteSkill("Java", "Ramalamadingdong").getStatusCode());
//    assertNotNull(skillRepo.findByName("Java"));
//  }
//
//  @Test
//  public void testDeleteMigrateToSource() {
//    assertEquals(HttpStatus.BAD_REQUEST, skillController.deleteSkill("Java", "Java").getStatusCode());
//    assertNotNull(skillRepo.findByName("Java"));
//  }
//
//  @Test
//  public void testEditValid() {
//    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "foobar").getStatusCode());
//    assertNull(skillRepo.findByName("Java"));
//    assertNotNull(skillRepo.findByName("foobar"));
//  }
//
//  @Test
//  public void testEditOldUnknown() {
//    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill("foobar", "barfoo").getStatusCode());
//    assertNull(skillRepo.findByName("barfoo"));
//    assertNull(skillRepo.findByName("foobar"));
//  }
//
//  @Test
//  public void testEditOldEmpty() {
//    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill("", "barfoo").getStatusCode());
//    assertNull(skillRepo.findByName(""));
//    assertNull(skillRepo.findByName("foobar"));
//  }
//
//  @Test
//  public void testEditOldNull() {
//    assertEquals(HttpStatus.NOT_FOUND, skillController.updateSkill(null, "barfoo").getStatusCode());
//    assertNull(skillRepo.findByName("barfoo"));
//  }
//
//  @Test
//  public void testEditNewExisting() {
//    assertEquals(HttpStatus.BAD_REQUEST, skillController.updateSkill("Java", "COBOL").getStatusCode());
//    assertNotNull(skillRepo.findByName("Java"));
//  }
//
//  @Test
//  public void testEditNewEmptyString() {
//    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "").getStatusCode());
//    assertNotNull(skillRepo.findByName("Java"));
//    assertNull(skillRepo.findByName(""));
//  }
//
//  @Test
//  public void testEditNewNull() {
//    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", null).getStatusCode());
//    assertNotNull(skillRepo.findByName("Java"));
//  }
//
//  @Test
//  public void testEditOldHasSpace() {
//    assertEquals(HttpStatus.OK, skillController.updateSkill("Java ", "Foobar").getStatusCode());
//    assertNotNull(skillRepo.findByName("Foobar"));
//  }
//
//  @Test
//  public void testEditNewHasSpaceValid() {
//    assertEquals(HttpStatus.OK, skillController.updateSkill("Java", "Foobar ").getStatusCode());
//    assertNotNull(skillRepo.findByName("Foobar"));
//  }
//
//  @Test
//  public void testEditNewHasSpaceExisting() {
//    assertEquals(HttpStatus.BAD_REQUEST, skillController.updateSkill("Java", " COBOL").getStatusCode());
//    assertNotNull(skillRepo.findByName("Java"));
//  }
//
//  @Test
//  public void testEditKeepPersonalSkillsWithNull() {
//    var user = userRepo.findByEmailIgnoreCase("aaaaaa");
//    user.addUpdateSkill("Java", 3, 3, false);
//    userRepo.save(user);
//
//    skillController.updateSkill("Java", null);
//    assertEquals("aaaaaa", userRepo.findBySkill("Java").get(0).getEmail());
//  }
//
//  @Test
//  public void testEditKeepPersonalSkillsWithEmpty() {
//    var user = userRepo.findByEmailIgnoreCase("aaaaaa");
//    user.addUpdateSkill("Java", 3, 3, false);
//    userRepo.save(user);
//
//    skillController.updateSkill("Java", "");
//    assertEquals("aaaaaa", userRepo.findBySkill("Java").get(0).getEmail());
//  }
//
//  @Test
//  public void testEditRenamePersonalSkills() {
//    var user = userRepo.findByEmailIgnoreCase("aaaaaa");
//    user.addUpdateSkill("Java", 3, 3, false);
//    userRepo.save(user);
//
//    skillController.updateSkill("Java", "Foobar");
//    assertEquals("aaaaaa", userRepo.findBySkill("Foobar").get(0).getEmail());
//  }
//
//  @Test
//  public void testEditRenamePersonalSkillsWithSpace() {
//    var user = userRepo.findByEmailIgnoreCase("aaaaaa");
//    user.addUpdateSkill("Java", 3, 3, false);
//    userRepo.save(user);
//
//    skillController.updateSkill("Java", "Foobar ");
//    assertEquals("aaaaaa", userRepo.findBySkill("Foobar").get(0).getEmail());
//  }
//
//  @Test
//  public void testEditKeepSuggestions() {
//    skillController.updateSkill("Java", "");
//    assertEquals(1, skillRepo.findByName("COBOL").getSuggestions().size());
//    assertEquals("Java", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
//  }
//
//  @Test
//  public void testEditRenameSuggestions() {
//    skillController.updateSkill("Java", "Foobar");
//    assertEquals(1, skillRepo.findByName("COBOL").getSuggestions().size());
//    assertEquals("Foobar", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
//  }
//
//  @Test
//  public void testEditRenameSuggestionsWithSpace() {
//    skillController.updateSkill("Java", "   Foobar ");
//    assertEquals(1, skillRepo.findByName("COBOL").getSuggestions().size());
//    assertEquals("Foobar", skillRepo.findByName("COBOL").getSuggestions().get(0).getName());
//  }
//
//}
