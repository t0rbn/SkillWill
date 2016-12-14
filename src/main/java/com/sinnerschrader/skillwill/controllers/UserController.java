package com.sinnerschrader.skillwill.controllers;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sinnerschrader.skillwill.mock.MockData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Users", description="User management and search")
@Controller
public class UserController {

	private MockData mockData;

	@PostConstruct
	private void initMock() {
		this.mockData = new MockData();
	}

	/**
	 *  List all users
	 */
	@ApiOperation(value = "search users", nickname = "search users", notes = "Search users.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "search", value = "Names of skills to search, separated by ','", paramType="query", required = false),
	})
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public ResponseEntity<String> getUsers(@RequestParam(required = false) String search) {
		if (StringUtils.isEmpty(search)) {
			return new ResponseEntity<String>(mockData.allUsers.toString(), HttpStatus.OK);
		}

		return new ResponseEntity<String>(mockData.someUsers.toString(), HttpStatus.OK);
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
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/users/{user}", method = RequestMethod.GET)
	public ResponseEntity<String> getUser(@PathVariable String user) {
		return new ResponseEntity<String>(mockData.foobar.toString(), HttpStatus.OK);
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
		@ApiImplicitParam(name = "skill", value = "Name of skill", paramType="form", required = true),
		@ApiImplicitParam(name = "skill_level", value = "Level of skill", paramType="form", required = true),
		@ApiImplicitParam(name = "will_level", value = "Level of will", paramType="form", required = true)
	})
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/users/{user}/skills", method = RequestMethod.POST)
	public ResponseEntity<String> modifiySkills(@PathVariable String user, @RequestParam("skill") String skill, @RequestParam("skill_level") String skill_level,@RequestParam("will_level") String will_level) {
		JSONObject returnStatus = new JSONObject();
		returnStatus.put("status", "success");
		return new ResponseEntity<String>(returnStatus.toString(), HttpStatus.OK);
	}

}
