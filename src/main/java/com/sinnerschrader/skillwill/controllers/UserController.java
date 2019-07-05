package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.services.SessionService;
import com.sinnerschrader.skillwill.services.SkillService;
import com.sinnerschrader.skillwill.services.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

  private final SessionService sessionService;

  private final SkillService skillService;

  @Autowired
  public UserController(UserService userService, SessionService sessionService, SkillService skillService) {
    this.userService = userService;
    this.sessionService = sessionService;
    this.skillService = skillService;
  }

  /**
   * Search for users with specific skills / list all users if no search query is specified
   */
  @ApiOperation(value = "search users", nickname = "search users", notes = "Search users.")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "skills", value = "Names of skills to search, separated by ','", paramType = "query", required = false),
  })
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
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @RequestMapping(path = "/users/{id}", method = RequestMethod.GET)
  public ResponseEntity<User> getUser(@PathVariable String id) {
    return new ResponseEntity<>(userService.getUser(id), HttpStatus.OK);
  }

  /**
   * modify users's skills
   */
  @ApiOperation(value = "modify skill", nickname = "modify skills", notes = "Create or edit a skill of a user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "_oauth2_proxy", value = "session token of the current user", paramType = "cookie", required = true),
    @ApiImplicitParam(name = "skill", value = "Name of skill", paramType = "form", required = true),
    @ApiImplicitParam(name = "skill_level", value = "Level of skill", paramType = "form", required = true),
    @ApiImplicitParam(name = "will_level", value = "Level of will", paramType = "form", required = true),
    @ApiImplicitParam(name = "mentor", value = "Mentor flag", paramType = "form", required = true, dataType = "Boolean")
  })
  @RequestMapping(path = "/users/{id}/skills", method = RequestMethod.POST)
  public ResponseEntity<String> updateSkills(@PathVariable String id,
    @RequestParam("skill") String skill, @RequestParam("skill_level") String skill_level,
    @RequestParam("will_level") String will_level, @RequestParam("mentor") boolean mentor, @CookieValue("_oauth2_proxy") String oAuthToken) {
    sessionService.validateForUserId(oAuthToken, id);
    userService.updateSkills(id, skill, Integer.parseInt(skill_level), Integer.parseInt(will_level), mentor);
    return ResponseEntity.ok().build();
  }

  /**
   * delete user's skill
   */
  @ApiOperation(value = "remove skill", nickname = "remove skills", notes = "remove a skill from a user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "_oauth2_proxy", value = "session token of the current user", paramType = "cookie", required = true),
    @ApiImplicitParam(name = "skill", value = "Name of skill", paramType = "query", required = true),
  })
  @RequestMapping(path = "/users/{id}/skills", method = RequestMethod.DELETE)
  public ResponseEntity removeSkill(@PathVariable String id,
    @RequestParam("skill") String skill, @CookieValue("_oauth2_proxy") String oAuthToken) {
    sessionService.validateForUserId(oAuthToken, id);
    userService.removeSkills(id, skill);
    logger.info("Successfully deleted {}'s skill {}", id, skill);
    return ResponseEntity.ok().build();
  }

  /**
   * Get users with similar skill sets
   */
  @ApiOperation(value = "get similar", nickname = "get similar", notes = "get users with similar skills sets")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "count", value = "number of users to find (max)", paramType = "query", defaultValue = "10"),
  })
  @RequestMapping(path = "/users/{id}/similar", method = RequestMethod.GET)
  public ResponseEntity<List<User>> getSimilar(@PathVariable String id,
    @RequestParam(value = "count", required = false) Integer count) {

    List<User> similar = userService.getSimilar(id, count);
    logger.debug("Successfully found {} users similar to {}", similar.size(), id);
    return new ResponseEntity<>(similar, HttpStatus.OK);
  }

}
