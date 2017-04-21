package com.sinnerschrader.skillwill.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SessionRepository;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.session.Session;
import com.unboundid.ldap.sdk.LDAPException;
import java.io.IOException;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test for UserController
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserControllerTest {

  private static final Logger logger = LoggerFactory.getLogger(LoginControllerTest.class);

  @Autowired
  private UserController userController;

  @Autowired
  private PersonRepository personRepo;

  @Autowired
  private SkillRepository skillRepo;

  @Autowired
  private SessionRepository sessionRepo;

  @Autowired
  private EmbeddedLdap embeddedLdap;

  @Before
  public void setUp() throws LDAPException, IOException {
    embeddedLdap.reset();

    skillRepo.deleteAll();
    skillRepo.insert(new KnownSkill("Java", "icon descriptor"));

    sessionRepo.deleteAll();
    Session session = new Session("abc123", "foobar", new Date());
    session.renewSession(60);
    sessionRepo.insert(session);

    personRepo.deleteAll();
    Person foobar = new Person("foobar");
    foobar.addUpdateSkill("Java", 2, 3);
    personRepo.insert(foobar);
  }

  @Test
  public void testGetUserValid() throws JSONException {
    logger.debug("Testing UserController: get valid user");

    ResponseEntity<String> res = userController.getUser("foobar");
    assertEquals(HttpStatus.OK, res.getStatusCode());

    assertTrue(new JSONObject(res.getBody()).has("id"));
    assertTrue(new JSONObject(res.getBody()).get("id").equals("foobar"));

    assertTrue(new JSONObject(res.getBody()).has("firstName"));
    assertTrue(new JSONObject(res.getBody()).get("firstName").equals("Fooberius"));

    assertTrue(new JSONObject(res.getBody()).has("lastName"));
    assertTrue(new JSONObject(res.getBody()).get("lastName").equals("Barblub"));
  }

  @Test
  public void testGetUserInvalid() {
    logger.debug("Testing UserController: get invalid user");
    assertEquals(HttpStatus.NOT_FOUND, userController.getUser("barfoo").getStatusCode());
  }

  @Test
  public void testGetUsersValid() throws JSONException {
    logger.debug("Testing UserController: get valid users");
    ResponseEntity<String> res = userController.getUsers("Java", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getString(0));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("foobar", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersSkillsEmpty() throws JSONException {
    logger.debug("Testing UserController: get users with empty skill");
    ResponseEntity<String> res = userController.getUsers("", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertFalse(new JSONObject(res.getBody()).has("searched"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("foobar", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersNoFitnessInEmptySearch() throws JSONException {
    logger.debug("Testing UserController: get users with empty skills -> no fitness in JSON");
    ResponseEntity<String> res = userController.getUsers("", "Hamburg");
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersLocationEmpty() throws JSONException {
    logger.debug("Testing UserController: get users for empty location");
    ResponseEntity<String> res = userController.getUsers("Java", "");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getString(0));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("foobar", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersSkillsEmptyLocationEmpty() throws JSONException {
    logger.debug("Testing UserController: get users for empty skill and empty location");
    ResponseEntity<String> res = userController.getUsers("", "");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertFalse(new JSONObject(res.getBody()).has("searched"));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("foobar", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertFalse(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersSkillUnknown() throws JSONException {
    logger.debug("Testing UserController: get users with unknown skill");
    ResponseEntity<String> res = userController.getUsers("Java, IAmUnknown", "Hamburg");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testGetUsersIgnoreSkillCase() throws JSONException {
    logger.debug("Testing UserController: get users - skill case insensitive");
    ResponseEntity<String> res = userController.getUsers("JaVa", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getString(0));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("foobar", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersIgnoreNonAlphanumerics() throws JSONException {
    logger.debug("Testing UserController: get users - skill case insensitive");
    ResponseEntity<String> res = userController.getUsers("j#a)_V®a", "Hamburg");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(new JSONObject(res.getBody()).has("searched"));
    assertEquals("Java", new JSONObject(res.getBody()).getJSONArray("searched").getString(0));
    assertEquals(1, new JSONObject(res.getBody()).getJSONArray("results").length());
    assertEquals("foobar", new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
    assertTrue(new JSONObject(res.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
  }

  @Test
  public void testGetUsersLocationUnknown() throws JSONException {
    logger.debug("Testing UserController: get users with unknown location");
    ResponseEntity<String> res = userController.getUsers("Java", "IAmUnknown");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(0, new JSONObject(res.getBody()).getJSONArray("results").length());
  }

  @Test
  public void testModifySkillsValid() {
    logger.debug("Testing UserController: modify skill");
    ResponseEntity<String> res = userController.updateSkills("foobar", "Java", "3", "0", "abc123");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getSkillLevel());
    assertEquals(0, personRepo.findById("foobar").getSkills().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsLevelsZero() {
    logger.debug("Testing UserController: modify skill with both levels zero");
    ResponseEntity<String> res = userController.updateSkills("foobar", "Java", "0", "0", "abc123");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testModifySkillsLevelOverMax() {
    logger.debug("Testing UserController: modify skill with both level over max");
    ResponseEntity<String> res = userController.updateSkills("foobar", "Java", "0", "4", "abc123");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testModifySkillsSessionInvalid() {
    logger.debug("Testing UserController: modify user with invalid session");
    ResponseEntity<String> res = userController.updateSkills("foobar", "Java", "0", "0", "InvalidSession");
    assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
    assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel());
    assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsUserUnknown() {
    logger.debug("Testing UserController: modify skills for unknown user");

    sessionRepo.deleteAll();
    Session session = new Session("2342", "IAmUnknown", new Date());
    session.renewSession(60);
    sessionRepo.insert(session);

    ResponseEntity<String> res = userController.updateSkills("IAmUnknown", "Java", "0", "0", "2342");
    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel());
    assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsSkillUnknown() {
    logger.debug("Testing UserController: modify skill for unknown skill");
    ResponseEntity<String> res = userController.updateSkills("foobar", "UnknownSkill", "0", "0", "abc123");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel());
    assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsSkillLevelOutOfRange() {
    logger.debug("Testing UserController: modify skill with skill out of range");
    ResponseEntity<String> res = userController.updateSkills("foobar", "Java", "5", "0", "abc123");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel());
    assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel());
  }

  @Test
  public void testModifySkillsWillLevelOutOfRange() {
    logger.debug("Testing UserController: modify skill with will out of range");
    ResponseEntity<String> res = userController.updateSkills("foobar", "Java", "0", "5", "abc123");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel());
    assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel());
  }

  @Test
  public void testRemoveSkill() {
    logger.debug("Testing UserController: remove skill");
    ResponseEntity<String> res = userController.removeSkill("foobar", "Java", "abc123");
    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  public void testRemoveSkillSkillUnknown() {
    logger.debug("Testing UserController: remove skill");
    ResponseEntity<String> res = userController.removeSkill("foobar", "UNKNOWN", "abc123");
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  public void testRemoveSkillUserUnknown() {
    logger.debug("Testing UserController: remove skill");
    ResponseEntity<String> res = userController.removeSkill("IAmUnknown", "Java", "abc123");
    assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
  }

  @Test
  public void testRemoveSkillUserUnauthorized() {
    logger.debug("Testing UserController: remove skill");
    ResponseEntity<String> res = userController.removeSkill("foobar", "Java", "IAmUnknown");
    assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
  }

  @Test
  public void testSetCommentValid() throws JSONException {
    logger.debug("Testing Usercontroller: set valid comment");
    ResponseEntity<String> res = userController.updateDetails("foobar", "abc123", "insert comment here");
    assertEquals(HttpStatus.OK, res.getStatusCode());

    res = userController.getUser("foobar");
    assertEquals("insert comment here", new JSONObject(res.getBody()).getString("comment"));
  }

  @Test
  public void testSetCommentUnicode() throws JSONException {
    logger.debug("Testing Usercontroller: set unicode comment");
    ResponseEntity<String> res = userController.updateDetails("foobar", "abc123", "本产品可能含有网络的痕迹");
    assertEquals(HttpStatus.OK, res.getStatusCode());

    res = userController.getUser("foobar");
    assertEquals("本产品可能含有网络的痕迹", new JSONObject(res.getBody()).getString("comment"));
  }

  @Test
  public void testSetCommentEmpty() throws JSONException {
    logger.debug("Testing Usercontroller: set empty comment");
    ResponseEntity<String> res = userController.updateDetails("foobar", "abc123", "");
    assertEquals(HttpStatus.OK, res.getStatusCode());

    res = userController.getUser("foobar");
    assertFalse(new JSONObject(res.getBody()).has("comment"));
  }

  @Test
  public void testSetCommentNull() throws JSONException {
    logger.debug("Testing Usercontroller: update details, ignore null");
    userController.updateDetails("foobar", "abc123", "insert comment here");
    ResponseEntity<String> res = userController.updateDetails("foobar", "abc123", null);
    assertEquals(HttpStatus.OK, res.getStatusCode());

    res = userController.getUser("foobar");
    assertFalse(new JSONObject(res.getBody()).has("comment"));
  }

  @Test
  public void testSetCommentUserNotLoggedIn() {
    logger.debug("Testing Usercontroller: update details with unauthorized user");
    ResponseEntity<String> res = userController.updateDetails("foobar", "ThisIsNotASessionKey", "comment");
    assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
  }

  @Test
  public void testGetSimilarUser() throws JSONException {
    logger.debug("Testing Usercontroller: get similar users");

    Person p1 = new Person("abc");
    p1.addUpdateSkill("Java", 1, 2);
    p1.addUpdateSkill(".NET", 3, 2);
    p1.addUpdateSkill("Text", 1, 3);
    personRepo.insert(p1);

    Person p2 = new Person("def");
    p2.addUpdateSkill("Java", 3, 2);
    personRepo.insert(p2);

    Person p3 = new Person("ghi");
    p3.addUpdateSkill("Java", 1, 0);
    p3.addUpdateSkill(".NET", 3, 2);
    personRepo.insert(p3);

    ResponseEntity<String> res = userController.getSimilar("abc", 1);
    assertEquals(1, new JSONArray(res.getBody()).length());
    assertEquals("ghi", new JSONArray(res.getBody()).getJSONObject(0).getString("id"));
  }

  @Test
  public void testGetSimilarUserNotFound() {
    logger.debug("Testing Usercontroller: get similar users for unknown user");
    ResponseEntity<String> res = userController.getSimilar("IAmUnknown", 42);
    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  public void testGetSimilarUserCountNegative() {
    logger.debug("Testing Usercontroller: get similar users with negative count");
    ResponseEntity<String> res = userController.getSimilar("foobar", -1);
    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

}
