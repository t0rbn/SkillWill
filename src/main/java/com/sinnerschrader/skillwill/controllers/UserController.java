package com.sinnerschrader.skillwill.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Users", description="User management and search")
@Controller
public class UserController {

	/**
	 *  List all users
	 */
	@ApiOperation(value = "search users", nickname = "search users", notes = "Search users.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "search", value = "Names of skills to search, separated by ','", paramType="form", required = true),
	})
	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public ResponseEntity<String> getUsers() {
		return new ResponseEntity<String>("list of all users (complete user objects)", HttpStatus.NOT_IMPLEMENTED);
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
		return new ResponseEntity<String>("All info about user " + user, HttpStatus.NOT_IMPLEMENTED);
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
		@ApiImplicitParam(name = "skill_id", value = "ID of skill", paramType="form", required = true),
		@ApiImplicitParam(name = "skill_level", value = "Level of skill", paramType="form", required = true),
		@ApiImplicitParam(name = "will_level", value = "Level of will", paramType="form", required = true)
	})
	@RequestMapping(path = "/users/{user}/skills", method = RequestMethod.POST)
	public ResponseEntity<String> modifiySkills(@PathVariable String user, @RequestParam("skill_id") String skill_id, @RequestParam("skill_level") String skill_level,@RequestParam("will_level") String will_level) {
		return new ResponseEntity<String>("change " + user + "s skill " + skill_id, HttpStatus.NOT_IMPLEMENTED);
	}

}
