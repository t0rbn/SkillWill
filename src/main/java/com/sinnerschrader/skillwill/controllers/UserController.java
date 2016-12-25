package com.sinnerschrader.skillwill.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sinnerschrader.skillwill.ldap.LdapSync;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.person.FitnessScore;
import com.sinnerschrader.skillwill.person.FitnessScoreComparator;
import com.sinnerschrader.skillwill.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.session.SessionManager;
import com.sinnerschrader.skillwill.skills.KnownSkill;
import com.sinnerschrader.skillwill.skills.PersonalSkill;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller handling /users/{foo}
 *
 * @author torree
 */
@Api(tags = "Users", description = "User management and search")
@Controller
@Scope("prototype")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private SkillsRepository skillRepo;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private LdapSync ldapSync;

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
    @CrossOrigin("http://localhost:8888")
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public ResponseEntity<String> getUsers(@RequestParam(required = false) String skills, @RequestParam(required = false) String location) {
        List<Person> matches = new ArrayList<Person>();

        if (StringUtils.isEmpty(skills)) {
            matches = personRepo.findAll();

            // Sync Ldap Details if empty
            for (Person p : matches) {
                if (p.getLdapDetails() == null) {
                    ldapSync.syncUser(p);
                }
            }

            // Filter Not searched locations from matches
            if (!StringUtils.isEmpty(location)) {
                matches = matches.stream()
                        .filter(p -> p.getLdapDetails().getLocation().equals(location))
                        .collect(Collectors.toList());
            }

            JSONArray arr = new JSONArray(matches.stream()
                    .map(p -> p.toJSON())
                    .collect(Collectors.toList())
            );

            logger.debug("Successfully searched for empty skills query: returning all users {}", !StringUtils.isEmpty(location) ? "from " + location : "");
            return new ResponseEntity<String>(arr.toString(), HttpStatus.OK);
        }

        List<String> searchItems = new ArrayList<String>(Arrays.asList(skills.split("\\s*,\\s*")));

        // Check if all searchItems are known Skills
        for (String s : searchItems) {
            if (skillRepo.findByName(s) == null) {
                logger.debug("Failed to search for {}: no matching skill found", s);
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

        // Sync Ldap Details if empty
        ldapSync.syncUsers(matches.stream()
                .filter(p -> p.getLdapDetails() == null)
                .collect(Collectors.toList())
        );

        // Filter for searched location
        if (!StringUtils.isEmpty(location)) {
            matches = matches.stream()
                    .filter(p -> (p.getLdapDetails().getLocation() != null && p.getLdapDetails().getLocation().equals(location)))
                    .collect(Collectors.toList());
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

        logger.debug("Successfully searched for skill(s) {}: returning {} users {}", skills.toString(), matches.size(), !StringUtils.isEmpty(location) ? "from " + location : "");
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
        Person p = personRepo.findById(user);

        if (p == null) {
            logger.info("Failed to return user {}: not found", user);
            StatusJSON json = new StatusJSON("user not found", HttpStatus.NOT_FOUND);
            return new ResponseEntity<String>(json.toString(), HttpStatus.NOT_FOUND);
        }

        // Sync Ldap Details if empty
        if (p.getLdapDetails() == null) {
            ldapSync.syncUser(p);
        }

        logger.debug("Successfully searched for {}", user);
        return new ResponseEntity<String>(p.toJSON().toString(), HttpStatus.OK);
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
    @CrossOrigin("http://localhost:8888")
    @RequestMapping(path = "/users/{user}/skills", method = RequestMethod.POST)
    public ResponseEntity<String> modifiySkills(@PathVariable String user, @RequestParam("skill") String skill, @RequestParam("skill_level") String skill_level, @RequestParam("will_level") String will_level, @RequestParam("session") String sessionKey) {
        if (!sessionManager.checkSession(user, sessionKey)) {
            logger.debug("Failed to modify {}'s skills: not logged in", user);
            StatusJSON json = new StatusJSON("user not logged in", HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<String>(json.toString(), HttpStatus.UNAUTHORIZED);
        }

        Person person = personRepo.findById(user);

        if (person == null) {
            // If this snippet gets executed, the users is not found in de DB, but there's a valid session.
            // Your're fucked
            logger.error("Failed to modify {}'s skills: user not found in DB, but session is valid", user);
            StatusJSON json = new StatusJSON("user not found", HttpStatus.NOT_FOUND);
            return new ResponseEntity<String>(json.toString(), HttpStatus.NOT_FOUND);
        }

        if (skillRepo.findByName(skill) == null) {
            logger.debug("Failed to modify {}'s skills: skill not found", user);
            StatusJSON json = new StatusJSON("skill not known", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        try {
            person.addUpdateSkill(new PersonalSkill(skill, Integer.parseInt(skill_level), Integer.parseInt(will_level)));
        } catch (IllegalArgumentException e) {
            logger.debug("Failed to modify {}'s skills: new value not in range", user);
            StatusJSON json = new StatusJSON("skill/will level must be in range 0-3", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        personRepo.save(person);

        logger.debug("Successfully modified {}'s skills", user);
        StatusJSON json = new StatusJSON("success", HttpStatus.OK);
        return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
    }

}
