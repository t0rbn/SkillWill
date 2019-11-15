package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.skills.UserSkill;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class User {

  @Id
  private String id;

  private String email;

  private String displayName;

  private List<UserSkill> skills;

  // Der Name ist scheiße, aber dies das wissenschaftlicher Anspruch DAS MUSS SO HEISSEN my ass
  @Transient
  private FitnessScore fitnessScore;

  @Version
  private Long version;

  public User() {
    this(null, null);
  }

  public User(String email) {
    this(email, email);
  }

  public User(String email, String name) {
    this.id = UUID.randomUUID().toString();
    this.email = email;
    this.displayName = name;
    this.skills = new ArrayList<>();
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<UserSkill> getSkills() {
    return new ArrayList<>(this.skills);
  }

  public void setSkills(List<UserSkill> skills) {
    this.skills = skills;
  }

  public UserSkill getSkill(String name) {
    return this.skills.stream()
        .filter(s -> s.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public boolean hasSkill(String skill) {
    return this.getSkill(skill) != null;
  }

  public void addUpdateSkill(String name, int skillLevel, int willLevel, boolean mentor) {
    try {
      removeSkill(name);
    } catch (SkillNotFoundException e) {
      // user doesn't have skill yet -> add new skill
    }
    this.skills.add(new UserSkill(name, skillLevel, willLevel, mentor));
  }

  public void removeSkill(String name) throws SkillNotFoundException {
    var toRemove = skills.stream()
        .filter(s -> s.getName().equals(name))
        .findAny()
        .orElseThrow(() -> new SkillNotFoundException("user does not have skill"));
    skills.remove(toRemove);
  }

  public void setFitnessScore(Collection<Skill> searchedSkills, FitnessScoreProperties props) {
    this.fitnessScore = new FitnessScore(this, searchedSkills, props);
  }

  public Double getFitnessScoreValue() {
    if (this.fitnessScore == null) {
      return null;
    }
    return this.fitnessScore.getValue();
  }

  public String getId() {
    return this.id;
  }

}
