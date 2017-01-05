package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.services.SkillService;
import io.swagger.annotations.*;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Controller handling /skills/{foo}
 *
 * @author torree
 */
@Api(tags = "Skills", description = "Manage all skills")
@Controller
@CrossOrigin("http://localhost:8888")
@Scope("prototype")
public class SkillController {

	@Autowired
	private SkillService skillService;

	/**
	 * get/suggest skills based on search query
	 * -> can be used for autocompletion when user started typing
	 */
	@ApiOperation(value = "suggest skills", nickname = "suggest skills", notes = "suggest skills")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "search", value = "Name to search", paramType = "query", required = false),
	})
	@RequestMapping(path = "/skills", method = RequestMethod.GET)
	public ResponseEntity<String> getSkills(@RequestParam(required = false) String search) {
		return new ResponseEntity<>(new JSONArray(skillService.getSkills(search).stream()
				.map(s -> s.getName())
				.collect(Collectors.toList())
		).toString(), HttpStatus.OK);
	}

	/**
	 * suggest next skill to enter
	 * -> can be used to suggest user what skill to enter next
	 */
	@ApiOperation(value = "suggest next skill", nickname = "suggest next skill", notes = "suggest next skill")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "search", value = "Names of skills already entered, separated by comma", paramType = "query", required = true),
	})
	@RequestMapping(path = "/skills/next", method = RequestMethod.GET)
	public ResponseEntity<String> getNext(@RequestParam(required = true) String search) {
		if (search == null || StringUtils.isEmpty(search)) {
			return new ResponseEntity<>(new StatusJSON("search must not be empty").toString(), HttpStatus.BAD_REQUEST);
		}

		try {
			String suggestedName = skillService.getSuggestedSkillName(Arrays.asList(search.split("\\s*,\\s*")));
			return new ResponseEntity<>(suggestedName, HttpStatus.OK);
		} catch (SkillNotFoundException e) {
			return new ResponseEntity<>(new StatusJSON("search contains unknown skill").toString(), HttpStatus.BAD_REQUEST);
		}
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
			@ApiImplicitParam(name = "name", value = "new skill's name", paramType = "form", required = true),
	})
	@RequestMapping(path = "/skills", method = RequestMethod.POST)
	public ResponseEntity<String> addSkill(@RequestParam String name) {
		try {
			skillService.createSkill(name);
			return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
		} catch (EmptyArgumentException | DuplicateSkillException e) {
			return new ResponseEntity<>(new StatusJSON(e.getMessage()).toString(), HttpStatus.BAD_REQUEST);
		}
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
	public ResponseEntity<String> deleteSkill(@PathVariable String skill) {
		try {
			skillService.deleteSkill(skill);
			return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
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
			@ApiImplicitParam(name = "name", value = "skill's new name", paramType = "form", required = true),
	})
	@RequestMapping(path = "/skills/{skill}", method = RequestMethod.PUT)
	public ResponseEntity<String> editSkill(@PathVariable String skill, @RequestParam(required = false) String name) {
		try {
			skillService.renameSkill(skill, name);
			return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
		} catch (DuplicateSkillException | IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}
