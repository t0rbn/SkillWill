package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.misc.OAuthHelper;
import com.sinnerschrader.skillwill.services.SkillService;
import com.sinnerschrader.skillwill.services.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Controller handling /users/{foo}
 *
 * @author torree
 */
@Api(tags = "Users", description = "User management and search")
@Controller
@CrossOrigin
@Scope("prototype")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  private final SkillService skillService;

  private final OAuthHelper oAuthHelper;

  @Autowired
  public UserController(UserService userService, SkillService skillService, OAuthHelper oAuthHelper) {
    this.userService = userService;
    this.skillService = skillService;
    this.oAuthHelper = oAuthHelper;
  }

  /**
   * Search for users with specific skills / list all users if no search query is specified
   */
  @ApiOperation(value = "search users", nickname = "search users", notes = "Search users.")
  @RequestMapping(path = "/users", method = RequestMethod.GET)
  public ResponseEntity getUsers(@RequestParam(required = false) String skills) {

    var skillSearchNames = StringUtils.isEmpty(skills)
      ? new ArrayList<String>()
      : Arrays.asList(skills.split(","));
    var searchResult = skillService.searchSkillsByNames(skillSearchNames);
    var foundUsers = userService.getUsers(searchResult);

    var json = new JSONObject();
    json.put("results", new JSONArray(foundUsers));
    json.put("searched", searchResult.mappingJson());

    skillService.registerSkillSearch(searchResult.mappedSkills());
    return new ResponseEntity<>(json.toString(), HttpStatus.OK);
  }


  /**
   * Get a user
   */
  @ApiOperation(value = "get info", nickname = "user info", notes = "Returns the user with the given id")
  @RequestMapping(path = "/users/{id}", method = RequestMethod.GET)
  public ResponseEntity<User> getUser(@PathVariable String id) {
    return new ResponseEntity<>(userService.getUser(id), HttpStatus.OK);
  }

  /**
   * modify users's skills
   */
  @ApiOperation(value = "modify skill", nickname = "modify skills", notes = "Create or edit a skill of a user")
  @RequestMapping(path = "/users/{id}/skills", method = RequestMethod.POST)
  public ResponseEntity<String> updateSkills(@PathVariable String id, @RequestParam("skill") String skill, @RequestParam("skill_level") String skill_level,
    @RequestParam("will_level") String will_level, @RequestParam("mentor") boolean mentor, OAuth2AuthenticationToken oauthToken) {
    oAuthHelper.validateForUserId(oauthToken, id);
    userService.updateSkills(id, skill, Integer.parseInt(skill_level), Integer.parseInt(will_level), mentor);
    return ResponseEntity.ok().build();
  }

  /**
   * delete user's skill
   */
  @ApiOperation(value = "remove skill", nickname = "remove skills", notes = "remove a skill from a user")
  @RequestMapping(path = "/users/{id}/skills", method = RequestMethod.DELETE)
  public ResponseEntity removeSkill(@PathVariable String id, @RequestParam("skill") String skill, OAuth2AuthenticationToken oauthToken) {
    oAuthHelper.validateForUserId(oauthToken, id);
    userService.removeSkills(id, skill);
    logger.info("Successfully deleted {}'s skill {}", id, skill);
    return ResponseEntity.ok().build();
  }

  /**
   * Get users with similar skill sets
   */
  @ApiOperation(value = "get similar", nickname = "get similar", notes = "get users with similar skills sets")
  @RequestMapping(path = "/users/{id}/similar", method = RequestMethod.GET)
  public ResponseEntity<List<User>> getSimilar(@PathVariable String id, @RequestParam(value = "count", required = false) Integer count) {

    List<User> similar = userService.getSimilar(id, count);
    logger.debug("Successfully found {} users similar to {}", similar.size(), id);
    return new ResponseEntity<>(similar, HttpStatus.OK);
  }

  @DeleteMapping(path = "/users/{id}")
  public ResponseEntity deleteUserById(@PathVariable String id) {

    userService.deleteUserById(id);

    return ResponseEntity.ok().build();
  }
}
