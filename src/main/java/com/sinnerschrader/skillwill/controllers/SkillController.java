package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.services.SkillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Collections;
import java.util.List;

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

@Api(tags = "Skills")
@Controller
@CrossOrigin
@Scope("prototype")
public class SkillController {

  private static final Logger logger = LoggerFactory.getLogger(SkillController.class);

  private final SkillService skillService;

  @Autowired
  public SkillController(SkillService skillService) {
    this.skillService = skillService;
  }

  /**
   * get/suggest skills based on search query -> can be used for autocompletion when user started
   * typing
   */
  @ApiOperation(value = "suggest skills", nickname = "suggest skills", notes = "suggest skills")
  @RequestMapping(path = "/skills", method = RequestMethod.GET)
  public ResponseEntity<List<Skill>> getSkills(@RequestParam(required = false) String search,
    @RequestParam(required = false, defaultValue = "-1") int count) {

    return new ResponseEntity<>(skillService.findSkills(search, count), HttpStatus.OK);
  }

  /**
   * Get a skill by its name
   */
  @ApiOperation(value = "get skill", nickname = "get skill")
  @RequestMapping(path = "/skills/{skill}", method = RequestMethod.GET)
  public ResponseEntity getSkill(@PathVariable(value = "skill") String name) {
    var skill = skillService.getSkillByName(name);
    if (skill == null) {
      logger.debug("Failed to get skill {}: not found", name);
      return ResponseEntity.notFound().build();
    }

    logger.debug("Successfully got skill {}", name);
    return new ResponseEntity<>(skill, HttpStatus.OK);
  }

  /**
   * suggest next skill to enter -> This is not the autocomplete for skill search (see
   * getSkills(true) for that) -> Recommender System: "Users who searched this also searched
   * for that"
   */
  @ApiOperation(value = "suggest next skill", nickname = "suggest next skill", notes = "suggest next skill")
  @RequestMapping(path = "/skills/next", method = RequestMethod.GET)
  public ResponseEntity getNext(@RequestParam(required = false) String search,
                                                 @RequestParam(defaultValue = "10") int count) {

    if (count < 1) {
      logger.debug("Failed to get suggestions for skills {}: count less than one", search);
      return ResponseEntity.badRequest().build();
    }

    try {
      List<String> searchItems = StringUtils.isEmpty(search) ? Collections.emptyList() : List.of(search.split("\\s*,\\s*"));
      var suggestionSkills = skillService.getSuggestionSkills(searchItems, count);
      logger.debug("Successfully got {} suggestions for search [{}]", suggestionSkills.size(), search);
      return new ResponseEntity<>(suggestionSkills, HttpStatus.OK);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to get suggestions for skills {}: serach contains inkown skill", search);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * create new skill
   */
  @ApiOperation(value = "add skill", nickname = "add skill", notes = "add a skill; Caution: parameter name is NOT the new skill's ID")
  @RequestMapping(path = "/skills", method = RequestMethod.POST)
  public ResponseEntity<String> addSkill(
    @RequestParam String name) {

    try {
      skillService.createSkill(name);
      logger.info("Successfully created new skill {}", name);
      return new ResponseEntity<>("success", HttpStatus.OK);
    } catch (EmptyArgumentException | DuplicateSkillException | SkillNotFoundException e) {
      logger.debug("Failed to create skill {}: argument empty or skill already exists", name);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * delete skill
   */
  @ApiOperation(value = "delete skill", nickname = "delete skill", notes = "parameter must be a valid skill Id")
  @RequestMapping(path = "/skills/{skill}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteSkill(@PathVariable String skill, @RequestParam(required = false) String migrateTo) {

    try {
      skillService.deleteSkill(skill, migrateTo);
      logger.info("Successfully deleted skill {}", skill);
      return ResponseEntity.ok().build();
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to delete skill {}: not found", skill);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      logger.debug("Failed to delete skill {}: illegal argument", skill);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * edit skill
   */
  @ApiOperation(value = "edit skill", nickname = "edit skill")
  @RequestMapping(path = "/skills/{skill}", method = RequestMethod.POST)
  public ResponseEntity<Void> updateSkill(@PathVariable String skill,
    @RequestParam(required = false) String name) {

    try {
      skillService.updateSkill(skill, name);
      return ResponseEntity.ok().build();
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to update skill {}: not found", skill);
      return ResponseEntity.notFound().build();
    } catch (DuplicateSkillException | IllegalArgumentException e) {
      logger.debug("Failed to update skill {}: illegal argument or skill already exists", skill);
      return ResponseEntity.badRequest().build();
    }
  }

}
