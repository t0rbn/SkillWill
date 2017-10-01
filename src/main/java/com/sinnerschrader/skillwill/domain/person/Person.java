package com.sinnerschrader.skillwill.domain.person;

import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.domain.skills.PersonalSkill;
import com.sinnerschrader.skillwill.domain.skills.SkillUtils;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.util.StringUtils;

/**
 * Class holding all information about a person
 *
 * @author torree
 */
public class Person {

  @Id
  private String id;
  private List<PersonalSkill> skills;
  private Role role;

  @Transient
  private FitnessScore fitnessScore;

  @Version
  private Long version;

  // LDAP Details will be updates regularly
  private PersonalLdapDetails ldapDetails;

  public Person(String id) {
    this.id = id;
    this.skills = new ArrayList<>();
    this.ldapDetails = null;
    this.fitnessScore = null;
    this.role = Role.USER;
  }

  public String getId() {
    return this.id;
  }

  public List<PersonalSkill> getSkillsExcludeHidden() {
    return this.skills.stream()
      .filter(s -> !s.isHidden())
      .collect(Collectors.toList());
  }

  public List<PersonalSkill>  getSkills() {
    return this.skills;
  }

  public PersonalSkill getSkill(String name) {
    return this.skills.stream()
        .filter(s -> s.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public PersonalSkill getSkillExcludeHidden(String name) {
    PersonalSkill skill = this.getSkill(name);
    return skill == null || skill.isHidden() ? null : skill;
  }

  public void deleteSkill(String name) {
    PersonalSkill skill = getSkill(name);
    if (skill != null) {
      this.skills.remove(skill);
    }
  }

  public PersonalLdapDetails getLdapDetails() {
    return this.ldapDetails;
  }

  public void setLdapDetails(PersonalLdapDetails ldapDetails) {
    this.ldapDetails = ldapDetails;
  }

  public void addUpdateSkill(String name, int skillLevel, int willLevel, boolean hidden, boolean mentor) {
    // Remove old skill if existing...
    Optional<PersonalSkill> existing = skills.stream()
        .filter(s -> s.getName().equals(SkillUtils.sanitizeName(name)))
        .findFirst();
    if (existing.isPresent()) {
      existing.get().setSkillLevel(skillLevel);
      existing.get().setWillLevel(willLevel);
      existing.get().setMentor(mentor);
      existing.get().setHidden(hidden);
    } else {
      this.skills.add(new PersonalSkill(name, skillLevel, willLevel, hidden, mentor));
    }
  }

  public void removeSkill(String name) {
    PersonalSkill skill = skills.stream()
        .filter(s -> s.getName().equals(name))
        .findAny()
        .orElseThrow(() -> new SkillNotFoundException("user does not have skill"));
    skills.remove(skill);
  }

  public void setFitnessScore(Collection<KnownSkill> searchedSkills, FitnessScoreProperties props) {
    this.fitnessScore = new FitnessScore(this, searchedSkills, props);
  }

  public double getFitnessScoreValue() {
    if (this.fitnessScore == null) {
      throw new IllegalStateException("no fitness score set");
    }

    return this.fitnessScore.getValue();
  }

  public JSONObject toJSON() {
    JSONObject obj = new JSONObject();
    obj.put("id", this.id);
    obj.put("role", this.role.toString());

    if (this.ldapDetails != null) {
      obj.put("firstName", ldapDetails.getFirstName());
      obj.put("lastName", ldapDetails.getLastName());
      obj.put("mail", ldapDetails.getMail());
      obj.put("phone", ldapDetails.getPhone());
      obj.put("location", ldapDetails.getLocation());
      obj.put("title", ldapDetails.getTitle());
    }

    if (this.fitnessScore != null) {
      obj.put("fitness", this.fitnessScore.getValue());
    }

    JSONArray skillsArr = new JSONArray();
    this.skills.stream()
      .filter(s -> !s.isHidden())
      .sorted(Comparator.comparing(PersonalSkill::getName))
      .map(PersonalSkill::toJSON)
      .forEach(skillsArr::put);

    obj.put("skills", skillsArr);
    return obj;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }
}
