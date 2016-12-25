package com.sinnerschrader.skillwill.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
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

import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.skills.KnownSkill;
import com.sinnerschrader.skillwill.skills.KnownSkillSuggestionComparator;
import com.sinnerschrader.skillwill.skills.SuggestionSkill;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller handling /skills/{foo}
 *
 * @author torree
 */
@Api(tags = "Skills", description = "Manage all skills")
@Controller
@Scope("prototype")
public class SkillController {

    private static Logger logger = LoggerFactory.getLogger(SkillController.class);

    @Autowired
    private SkillsRepository skillRepo;

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
    @CrossOrigin("http://localhost:8888")
    @RequestMapping(path = "/skills", method = RequestMethod.GET)
    public ResponseEntity<String> getSkills(@RequestParam(required = false) String search) {
        List<KnownSkill> skills;

        if (StringUtils.isEmpty(search)) {
            logger.debug("Suggesting autocompletion for empty search -> returning all skills");
            skills = skillRepo.findAll();
        } else {
            logger.debug("Suggesting autocomplection for {}", search);
            skills = new ArrayList<KnownSkill>();
            skills.addAll(skillRepo.findFuzzyByName(search));
            skills.sort(new KnownSkillSuggestionComparator(search));
        }

        JSONArray arr = new JSONArray();
        for (KnownSkill s : skills) {
            arr.put(s.getName());
        }

        return new ResponseEntity<String>(arr.toString(), HttpStatus.OK);
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
    @CrossOrigin("http://localhost:8888")
    @RequestMapping(path = "/skills/next", method = RequestMethod.GET)
    public ResponseEntity<String> getNext(@RequestParam(required = true) String search) {
        logger.debug("Suggestion next skill for {} ", search);

        // candidates = all SuggestionSkills including duplicates and crap
        List<SuggestionSkill> candidates = new ArrayList<SuggestionSkill>();

        // aggregated = candidates without duplicates/already known skills
        List<SuggestionSkill> aggregated = new ArrayList<SuggestionSkill>();

        // Create List of all known skills from search query
        List<KnownSkill> enteredSkills =
                new ArrayList<String>(Arrays.asList(search.split("\\s*,\\s*")))
                        .stream()
                        .map(s -> skillRepo.findByName(s))
                        .filter(s -> s != null)
                        .collect(Collectors.toList());

        // Create a list of NAMES of entered skills -> will come in handy
        final List<String> enteredSkillNames = enteredSkills.stream().map(s -> s.getName()).collect(Collectors.toList());

        // Adding all suggestions of all skills to canditates
        // -> candidates will contain duplicates and already entered skills
        for (KnownSkill skill : enteredSkills) {
            candidates.addAll(skill.getSuggestions());
        }

        // Remove already entered skills from candidates
        candidates = candidates.stream()
                .filter(c -> !enteredSkillNames.contains(c.getName()))
                .collect(Collectors.toList());

        // Aggregate candidates -> combine suggestion skills with same name
        for (SuggestionSkill candidate : candidates) {

            // List of all SuggestionSkills in aggregated mathcing to current candidate
            List<SuggestionSkill> existing = aggregated.stream()
                    .filter(s -> s.getName().equals(candidate.getName()))
                    .collect(Collectors.toList());

            if (existing.size() > 1) {
                // More than one element in aggregated matches current candidate
                // Since this list should hold aggregated SugggestionSkills, this shouldn't happen
                logger.error("Duplicate free list contains duplicates. Your're fucked");
                throw new IllegalStateException("Duplicate-Free aggregated list contains duplicates. Your're fucked");
            }

            if (!existing.isEmpty()) {
                // There's a matching suggestion -> summing up counts
                existing.get(0).setCount(existing.get(0).getCount() + candidate.getCount());
            } else {
                // No match found, add new SuggestionSkill
                aggregated.add(candidate);
            }
        }

        // Current top match SuggestionSkill
        SuggestionSkill recommended = new SuggestionSkill("", -1);

        // Finding SuggestionSkill with highest count in aggregated
        // -> this will be the final recommendation
        for (SuggestionSkill max : aggregated) {
            if (max.getCount() > recommended.getCount()) {
                recommended = max;
            }
        }

        return new ResponseEntity<String>(recommended.getName(), HttpStatus.OK);
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
    @CrossOrigin("http://localhost:8888")
    @RequestMapping(path = "/skills", method = RequestMethod.POST)
    public ResponseEntity<String> addSkill(@RequestParam String name) {
        if (skillRepo.findByName(name) != null) {
            logger.info("Failted to create skill {}: already existing", name);
            StatusJSON json = new StatusJSON("skill already exists", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(name)) {
            logger.info("Failed to create skill with empty name");
            StatusJSON json = new StatusJSON("skill cannot be empty", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        skillRepo.insert(new KnownSkill(name));

        logger.info("Successfully created skill {}: already existing", name);
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
        if (StringUtils.isEmpty(skill)) {
            logger.info("Failed to remove skill with empty name");
            StatusJSON json = new StatusJSON("skill cannot be empty", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        KnownSkill toRemove = skillRepo.findByName(skill);

        if (toRemove == null) {
            logger.info("Failed to remove skill {}: skill does not exist", skill);
            StatusJSON json = new StatusJSON("skill does not exist", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        skillRepo.delete(toRemove);

        for (KnownSkill s : skillRepo.findAll()) {
            s.deleteSuggestion(skill);
            skillRepo.save(s);
        }

        logger.info("Successfully removed skill {}", skill);
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
            @ApiImplicitParam(name = "name", value = "skill's new name", paramType = "form", required = true),
    })
    @RequestMapping(path = "/skills/{skill}", method = RequestMethod.PUT)
    public ResponseEntity<String> editSkill(@PathVariable String skill, @RequestParam(required = false) String name) {
        if (StringUtils.isEmpty(skill) || StringUtils.isEmpty(name)) {
            logger.info("Failed to rename skill: empty skill or name");
            StatusJSON json = new StatusJSON("skill or name cannot be empty", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        KnownSkill toEdit = skillRepo.findByName(skill);

        if (toEdit == null) {
            logger.debug("Failed to rename skill {}: does not exist", skill);
            StatusJSON json = new StatusJSON("skill does not exist", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        if (skillRepo.findByName(name) != null) {
            logger.debug("Failed to rename skill {}: new name already exists", skill);
            StatusJSON json = new StatusJSON("skill " + name + " already exists", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
        }

        skillRepo.delete(toEdit);
        skillRepo.insert(new KnownSkill(name, toEdit.getSuggestions()));

        for (KnownSkill s : skillRepo.findAll()) {
            s.renameSuggestion(skill, name);
            skillRepo.save(s);
        }

        logger.debug("Successfully renamed {} to {}", skill, name);
        StatusJSON json = new StatusJSON("success", HttpStatus.OK);
        return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
    }

}
