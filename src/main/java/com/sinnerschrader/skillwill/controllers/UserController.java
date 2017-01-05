package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.exceptions.UserNotFoundException;
import com.sinnerschrader.skillwill.domain.person.FitnessScoreProperties;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.domain.person.FitnessScore;
import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.services.SessionService;
import com.sinnerschrader.skillwill.services.SkillService;
import com.sinnerschrader.skillwill.services.UserService;
import io.swagger.annotations.*;
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
@CrossOrigin("http://localhost:8888")
@Scope("prototype")
public class UserController {

	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private SkillService skillService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private FitnessScoreProperties fitnessScoreProperties;

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
			@ApiImplicitParam(name = "location", value = "Location to filter results by", paramType = "query", required = false),
	})
	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public ResponseEntity<String> getUsers(@RequestParam(required = false) String skills, @RequestParam(required = false) String location) {
		skills = skills != null ? skills : "";
		location = location != null ? location : "";
		List<Person> matches;

		// Arrays.asList has fixed length, so add all to new List
		List<String> skillNameList = new ArrayList<>();
		if (!StringUtils.isEmpty(skills)) {
			skillNameList.addAll(Arrays.asList(skills.split("\\s*,\\s*")));
		}

		try {
			matches = userService.getUsers(skillNameList, location);
			skillService.registerSkillSearch(skillNameList);

			JSONArray arr = new JSONArray();
			for (Person p : matches) {
				JSONObject personObj = p.toJSON();
				personObj.put("fitness", new FitnessScore(p, skillNameList, fitnessScoreProperties).getValue());
				arr.put(personObj);
			}

			return new ResponseEntity<>(arr.toString(), HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
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
	@RequestMapping(path = "/users/{user}", method = RequestMethod.GET)
	public ResponseEntity<String> getUser(@PathVariable String user) {
		try {
			Person p = userService.getUser(user);
			return new ResponseEntity<>(p.toJSON().toString(), HttpStatus.OK);
		} catch (UserNotFoundException e) {
			StatusJSON json = new StatusJSON("user not found");
			return new ResponseEntity<>(json.toString(), HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * modify users's skills
	 */
	@ApiOperation(value = "modify skills", nickname = "modify skills", notes = "Create or edit a skill of a user")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "session", value = "users's active session key", paramType = "form", required = true),
			@ApiImplicitParam(name = "skill", value = "Name of skill", paramType = "form", required = true),
			@ApiImplicitParam(name = "skill_level", value = "Level of skill", paramType = "form", required = true),
			@ApiImplicitParam(name = "will_level", value = "Level of will", paramType = "form", required = true)
	})
	@RequestMapping(path = "/users/{user}/skills", method = RequestMethod.POST)
	public ResponseEntity<String> modifiySkills(@PathVariable String user, @RequestParam("skill") String skill, @RequestParam("skill_level") String skill_level, @RequestParam("will_level") String will_level, @RequestParam("session") String sessionKey) {
		if (!sessionService.checkSession(user, sessionKey)) {
			logger.debug("Failed to modify {}'s skills: not logged in", user);
			return new ResponseEntity<>(new StatusJSON("user not logged in").toString(), HttpStatus.UNAUTHORIZED);
		}

		try {
			userService.modifyUsersSkills(user, skill, Integer.parseInt(skill_level), Integer.parseInt(will_level));
			return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
		} catch (UserNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}
