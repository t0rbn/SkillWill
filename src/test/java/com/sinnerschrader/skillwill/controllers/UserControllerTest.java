package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SessionRepository;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.session.Session;
import com.sinnerschrader.skillwill.skills.KnownSkill;
import com.sinnerschrader.skillwill.skills.PersonalSkill;
import com.sinnerschrader.skillwill.testinfrastructure.EmbeddedLdap;
import com.unboundid.ldap.sdk.LDAPException;
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

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for UserController
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserControllerTest {
    public static Logger logger = LoggerFactory.getLogger(LoginControllerTest.class);

    @Autowired
    private UserController userController;

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private SkillsRepository skillRepo;

    @Autowired
    private SessionRepository sessionRepo;

    @Autowired
    private EmbeddedLdap embeddedLdap;

    @Before
    public void setUp() throws LDAPException, IOException {
        embeddedLdap.reset();

        skillRepo.deleteAll();
        skillRepo.insert(new KnownSkill("Java"));

        sessionRepo.deleteAll();
        Session session = new Session("abc123", "foobar", new Date());
        session.renewSession(60);
        sessionRepo.insert(session);

        personRepo.deleteAll();
        Person foobar = new Person("foobar");
        foobar.addUpdateSkill(new PersonalSkill("Java", 2, 3));
        personRepo.insert(foobar);
    }

    @Test
    public void testGetUserValid() throws JSONException {
        logger.debug("Testing UserController: get valid user");

        ResponseEntity<String> res = userController.getUser("foobar");
        assertTrue(res.getStatusCode() == HttpStatus.OK);

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
        assertTrue(userController.getUser("barfoo").getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetUsersValid() throws JSONException {
        logger.debug("Testing UserController: get valid users");
        ResponseEntity<String> res = userController.getUsers("Java", "Hamburg");
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertTrue(new JSONArray(res.getBody()).length() == 1);
        assertTrue(new JSONArray(res.getBody()).getJSONObject(0).has("id"));
        assertEquals("foobar", new JSONArray(res.getBody()).getJSONObject(0).getString("id"));
    }

    @Test
    public void testGetUsersSkillsEmpty() throws JSONException {
        logger.debug("Testing UserController: get users with empty skill");
        ResponseEntity<String> res = userController.getUsers("", "Hamburg");
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertTrue(new JSONArray(res.getBody()).length() == 1);
        assertTrue(new JSONArray(res.getBody()).getJSONObject(0).has("id"));
        assertEquals("foobar", new JSONArray(res.getBody()).getJSONObject(0).getString("id"));
    }

    @Test
    public void testGetUsersLocationEmpty() throws JSONException {
        logger.debug("Testing UserController: get users for empty location");
        ResponseEntity<String> res = userController.getUsers("Java", "");
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertTrue(new JSONArray(res.getBody()).length() == 1);
        assertTrue(new JSONArray(res.getBody()).getJSONObject(0).has("id"));
        assertEquals("foobar", new JSONArray(res.getBody()).getJSONObject(0).getString("id"));
    }

    @Test
    public void testGetUsersSkillsEmptyLocationEmpty() throws JSONException {
        logger.debug("Testing UserController: get users for empty skill and empty location");
        ResponseEntity<String> res = userController.getUsers("", "");
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertTrue(new JSONArray(res.getBody()).length() == 1);
        assertTrue(new JSONArray(res.getBody()).getJSONObject(0).has("id"));
        assertEquals("foobar", new JSONArray(res.getBody()).getJSONObject(0).getString("id"));
    }

    @Test
    public void testGetUSersSkillUnknown() {
        logger.debug("Testing UserController: get users with unknown skill");
        ResponseEntity<String> res = userController.getUsers("IAmUnknown", "Hamburg");
        assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetUsersLocationUnknown() {
        logger.debug("Testing UserController: get users with unknown location");
        ResponseEntity<String> res = userController.getUsers("Java", "IAmUnknown");
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertEquals("[]", res.getBody());
    }

    @Test
    public void testModifySkillsValid() {
        logger.debug("Testing UserController: modify skill");
        ResponseEntity<String> res = userController.modifiySkills("foobar", "Java", "0", "0", "abc123");
        assertTrue(res.getStatusCode() == HttpStatus.OK);
        assertEquals(0, personRepo.findById("foobar").getSkills().get(0).getSkillLevel().getInt());
        assertEquals(0, personRepo.findById("foobar").getSkills().get(0).getWillLevel().getInt());
    }

    @Test
    public void testModifySkillsSessionInvalid() {
        logger.debug("Testing UserController: modify user with invalid session");
        ResponseEntity<String> res = userController.modifiySkills("foobar", "Java", "0", "0", "InvalidSession");
        assertTrue(res.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel().getInt());
        assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel().getInt());
    }

    @Test
    public void testModifySkillsUserUnknown() {
        logger.debug("Testing UserController: modify skills for unknown user");

        sessionRepo.deleteAll();
        Session session = new Session("2342", "IAmUnknown", new Date());
        session.renewSession(60);
        sessionRepo.insert(session);

        ResponseEntity<String> res = userController.modifiySkills("IAmUnknown", "Java", "0", "0", "2342");
        assertTrue(res.getStatusCode() == HttpStatus.NOT_FOUND);
        assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel().getInt());
        assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel().getInt());
    }

    @Test
    public void testModifySkillsSkillUnknown() {
        logger.debug("Testing UserController: modify skill for unknown skill");
        ResponseEntity<String> res = userController.modifiySkills("foobar", "UnknownSkill", "0", "0", "abc123");
        assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
        assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel().getInt());
        assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel().getInt());
    }

    @Test
    public void testModifySkillsSkillLevelOutOfRange() {
        logger.debug("Testing UserController: modify skill with skill out of range");
        ResponseEntity<String> res = userController.modifiySkills("foobar", "Java", "5", "0", "abc123");
        assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
        assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel().getInt());
        assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel().getInt());
    }

    @Test
    public void testModifySkillsWillLevelOutOfRange() {
        logger.debug("Testing UserController: modift skill with will out of range");
        ResponseEntity<String> res = userController.modifiySkills("foobar", "Java", "0", "5", "abc123");
        assertTrue(res.getStatusCode() == HttpStatus.BAD_REQUEST);
        assertEquals(2, personRepo.findById("foobar").getSkills().get(0).getSkillLevel().getInt());
        assertEquals(3, personRepo.findById("foobar").getSkills().get(0).getWillLevel().getInt());
    }

}
