package com.sinnerschrader.skillwill.controllers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.skills.KnownSkill;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Skills", description = "Manage all skills")
@Controller
public class SkillController {

	@Autowired
	private SkillsRepository skillRepo;

	/**
	 * get/suggest skills
	 */
	@ApiOperation(value = "suggest skills", nickname = "suggest skills", notes = "suggest skills")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 500, message = "Failure")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "search", value = "Name to search", paramType="query", required = false),
	})
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/skills", method = RequestMethod.GET)
	public ResponseEntity<String> getSkills(@RequestParam(required = false) String search) {
		List<KnownSkill> skills;
		if (StringUtils.isEmpty(search)) {
			skills = skillRepo.findAll();
		} else {
			skills = new ArrayList<KnownSkill>();
			skills.addAll(skillRepo.findFuzzyByName(search));
		}

		JSONArray arr = new JSONArray();
		for (KnownSkill s : skills) {
			arr.put(s.getName());
		}

		return new ResponseEntity<String>(arr.toString(), HttpStatus.OK);
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
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/skills", method = RequestMethod.POST)
	public ResponseEntity<String> addSkill(@RequestParam String name) {
		if (skillRepo.findByName(name) != null) {
			StatusJSON json = new StatusJSON("skill already exists", HttpStatus.BAD_REQUEST);
			return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
		}

		skillRepo.insert(new KnownSkill(name));

		StatusJSON json = new StatusJSON("success", HttpStatus.OK);
		return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
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
		KnownSkill toRemove = skillRepo.findByName(skill);
		if (toRemove == null) {
			StatusJSON json = new StatusJSON("skill does not exist", HttpStatus.BAD_REQUEST);
			return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
		}

		skillRepo.delete(toRemove);

		StatusJSON json = new StatusJSON("success", HttpStatus.OK);
		return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
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
	public ResponseEntity<String> editSkill(@PathVariable String skill, @RequestParam(required = false) String name) {
		KnownSkill toEdit = skillRepo.findByName(skill);
		if (toEdit == null) {
			StatusJSON json = new StatusJSON("skill does not exist", HttpStatus.BAD_REQUEST);
			return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
		}

		if (skillRepo.findByName(name) != null) {
			StatusJSON json = new StatusJSON("skill " + name + " already exists", HttpStatus.BAD_REQUEST);
			return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
		}

		skillRepo.delete(toEdit);
		skillRepo.insert(new KnownSkill(name, toEdit.getSuggestions()));

		// Woohooo, this might be slow as fuck
		// TODO optimize
		for (KnownSkill s : skillRepo.findAll()) {
			s.renameSuggestions(skill, name);
			skillRepo.save(s);
		}

		StatusJSON json = new StatusJSON("success", HttpStatus.OK);
		return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
	}

}
