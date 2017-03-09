package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.services.SkillService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
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

	private static Logger logger = LoggerFactory.getLogger(SkillController.class);

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
		logger.debug("Successfully got autocompletion for {}", search);

		JSONArray skillsArr = new JSONArray(
				skillService.getSkills(search)
				.stream()
				.map(s -> s.toJSON())
				.collect(Collectors.toList())
		);
		return new ResponseEntity<>(skillsArr.toString(), HttpStatus.OK);
	}

	/**
	 * Get a skill by its name
	 */
	@ApiOperation(value = "get skill", nickname = "get skill")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@RequestMapping(path = "/skills/{skill}", method = RequestMethod.GET)
	public ResponseEntity<String> getSkill(@PathVariable(value = "skill") String name) {
		KnownSkill skill = skillService.getSkillByName(name);

		if (skill == null) {
			logger.debug("Failed to get skill {}: not found", name);
			return new ResponseEntity<>(new StatusJSON("skill not found").toString(), HttpStatus.NOT_FOUND);
		}

		logger.debug("Successfully got skill {}", name);
		return new ResponseEntity<>(skill.toJSON().toString(), HttpStatus.OK);
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
			@ApiImplicitParam(name = "count", value = "Count of recommendations to get", paramType = "query", defaultValue = "10")
	})
	@RequestMapping(path = "/skills/next", method = RequestMethod.GET)
	public ResponseEntity<String> getNext(@RequestParam String search, @RequestParam(defaultValue = "10") int count) {
		if (count < 0) {
			logger.debug("Failed to get suggestions for skills {}: parameter count less than zero", search);
			return new ResponseEntity<>(new StatusJSON("count must be a positive integer (or zero)").toString(), HttpStatus.BAD_REQUEST);
		}

		try {
			List<KnownSkill> suggestionSkills = skillService.getSuggestionSkills(Arrays.asList(search.split("\\s*,\\s*")), count);
			List<JSONObject> suggestionJsons = suggestionSkills.stream().map(KnownSkill::toJSON).collect(Collectors.toList());
			logger.debug("Successfully got {} suggestions for search {}", suggestionJsons.size(), search);
			return new ResponseEntity<>(new JSONArray(suggestionJsons).toString(), HttpStatus.OK);
		} catch (SkillNotFoundException e) {
			logger.debug("Failed to get suggestions for skills {}: serach contains inkown skill", search);
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
			@ApiImplicitParam(name = "icon_descriptor", value = "new skill's icon description", paramType = "form", required = true),
	})
	@RequestMapping(path = "/skills", method = RequestMethod.POST)
	public ResponseEntity<String> addSkill(@RequestParam String name, @RequestParam String icon_descriptor) {
		try {
			skillService.createSkill(name, icon_descriptor);
			logger.info("Successfully created new skill {}", name);
			return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
		} catch (EmptyArgumentException | DuplicateSkillException e) {
			logger.debug("Failed to create skill {}: argument empty or skill already exists", name);
			return new ResponseEntity<>(new StatusJSON(e.getMessage()).toString(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * delete skill
	 */
	@ApiOperation(value = "delete skill", nickname = "delete skill", notes = "parameter must be a valid skill Id")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@RequestMapping(path = "/skills/{skill}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteSkill(@PathVariable String skill) {
		try {
			skillService.deleteSkill(skill);
			logger.info("Successfully deleted skill {}", skill);
			return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
		} catch (SkillNotFoundException e) {
			logger.debug("Failed to delete skill {}: not found", skill);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException e) {
			logger.debug("Failed to delete skill {}: illegal argument");
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * edit skill
	 */
	@ApiOperation(value = "edit skill", nickname = "edit skill")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "name", value = "skill's new name", paramType = "form", required = true),
			@ApiImplicitParam(name = "icon_descriptor", value = "skill's new icon description", paramType = "form", required = true),
	})
	@RequestMapping(path = "/skills/{skill}", method = RequestMethod.PUT)
	public ResponseEntity<String> updateSkill(@PathVariable String skill, @RequestParam String name, @RequestParam String icon_descriptor) {
		try {
			skillService.updateSkill(skill, name, icon_descriptor);
			logger.info("Successfully updated skill {}", skill);
			return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
		} catch (SkillNotFoundException e) {
			logger.debug("Failed to update skill {}: not found", skill);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (DuplicateSkillException | IllegalArgumentException e) {
			logger.debug("Failed to update skill {}: illegal argument or skill already exists", skill);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}
