package com.sinnerschrader.skillwill.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Skills", description = "Manage all skills")
@Controller
public class SkillController {

	/**
	 * get/suggest skills
	 */
	@ApiOperation(value = "suggest skills", nickname = "suggest skills", notes = "suggest skills")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "search", value = "Name to search", paramType="form", required = true),
	})
	@RequestMapping(path = "/skills", method = RequestMethod.GET)
	public ResponseEntity<String> getSkills() {
		return new ResponseEntity<String>("List of all skills", HttpStatus.NOT_IMPLEMENTED);
	}

	/**
	 * create new skill
	 */
	@ApiOperation(value = "add skill", nickname = "add skill", notes = "add a skill; Caution: parameter name is NOT the new skill's ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "name", value = "new skill's name", paramType="form", required = true),
	})
	@RequestMapping(path = "/skills", method = RequestMethod.POST)
	public ResponseEntity<String> addSkill(@RequestParam String name) {
		return new ResponseEntity<String>("create skill " + name, HttpStatus.NOT_IMPLEMENTED);
	}

	/**
	 * get specific skill
	 */
	@ApiOperation(value = "get skill", nickname = "get a specific skill", notes = "parameter must be a valid skill ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 404, message = "Not Found"),
		@ApiResponse(code = 500, message = "Failure")
	})
	@RequestMapping(path = "/skills/{skill}", method = RequestMethod.GET)
	public ResponseEntity<String> getSkill(@RequestParam String skill) {
		return new ResponseEntity<String>("Get details about " + skill, HttpStatus.NOT_IMPLEMENTED);
	}

	/**
	 * delete skill
	 */
	@ApiOperation(value = "delete skill", nickname = "delete skill", notes = "parameter must be a valid skill Id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 404, message = "Not Found"),
		@ApiResponse(code = 500, message = "Failure")
	})
	@RequestMapping(path = "/skills/{skill}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteSkill(@RequestParam String skill) {
		return new ResponseEntity<String>("delete skill " + skill, HttpStatus.NOT_IMPLEMENTED);
	}

	/**
	 * edit skill
	 */
	@ApiOperation(value = "edit skill", nickname = "edit skill", notes = "currently only the skill's name can be edited")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 404, message = "Not Found"),
		@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "name", value = "skill's new name", paramType="form", required = true),
	})
	@RequestMapping(path = "/skills/{skill}", method = RequestMethod.PUT)
	public ResponseEntity<String> editSkill(@RequestParam String skill) {
		return new ResponseEntity<String>("edit skill " + skill, HttpStatus.NOT_IMPLEMENTED);
	}

}
