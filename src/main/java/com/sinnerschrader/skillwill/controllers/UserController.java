package com.sinnerschrader.skillwill.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
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
import com.sinnerschrader.skillwill.person.FitnessScore;
import com.sinnerschrader.skillwill.person.FitnessScoreComparator;
import com.sinnerschrader.skillwill.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.skills.KnownSkill;
import com.sinnerschrader.skillwill.skills.PersonalSkill;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Users", description="User management and search")
@Controller
public class UserController {

	@Autowired
	private PersonRepository personRepo;

	@Autowired
	private SkillsRepository skillRepo;

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
		List<Person> matches = new ArrayList<Person>();

		if (StringUtils.isEmpty(search)) {
			matches = personRepo.findAll();
			
			JSONArray arr = new JSONArray();
			for (Person person : matches) {
				arr.put(person.toJSON());
			}

			return new ResponseEntity<String>(arr.toString(), HttpStatus.OK);
			
		}

		List<String> searchItems = new ArrayList<String>(Arrays.asList(search.split("\\s*,\\s*")));

		// Check if all searchItems are known Skills
		for (String s : searchItems) {
			if (skillRepo.findByName(s) == null) {
				StatusJSON json = new StatusJSON("skill " + s + " not found", HttpStatus.BAD_REQUEST);
				return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
			}
		}


		// Split all search items into the first one by which the users will be retrieved from the DB,
		// and all others that will be used to filter the users
		String firstItem = searchItems.get(0);
		List<String> otherItems = new ArrayList<String>(searchItems);
		otherItems.remove(0);

		for (Person p : personRepo.findBySkill(firstItem)) {
			// Person p is a match, if it contains all other skills
			if (p.getSkills().stream().map(s -> s.getName()).collect(Collectors.toList()).containsAll(otherItems)) {
				matches.add(p);
			}
		}

		matches.sort(new FitnessScoreComparator(searchItems));

		// add the search to the knowledge base => refine next recommendations
		for (String s : searchItems) {
			KnownSkill skill = skillRepo.findByName(s);
			if (skill != null) {
				searchItems.stream().filter(t -> !s.equals(t)).forEach(t -> skill.incrementSuggestion(t));
				skillRepo.save(skill);
			}
		}

		JSONArray arr = new JSONArray();
		for (Person person : matches) {
			JSONObject personJSON = person.toJSON();
			personJSON.put("fitness", (new FitnessScore(person, searchItems).getValue()));
			arr.put(personJSON);
		}

		return new ResponseEntity<String>(arr.toString(), HttpStatus.OK);
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
		return new ResponseEntity<String>(personRepo.findById(user).toJSON().toString(), HttpStatus.OK);
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
		Person person = personRepo.findById(user);

		if (person == null) {
			StatusJSON json = new StatusJSON("user not found", HttpStatus.BAD_REQUEST);
			return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
		}

		if (skillRepo.findByName(skill) == null) {
			StatusJSON json = new StatusJSON("skill not known", HttpStatus.BAD_REQUEST);
			return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
		}

		person.addUpdateSkill(new PersonalSkill(skill, Integer.parseInt(skill_level), Integer.parseInt(will_level)));

		StatusJSON json = new StatusJSON("success", HttpStatus.OK);
		return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
	}

}
