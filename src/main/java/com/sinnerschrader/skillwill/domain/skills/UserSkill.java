package com.sinnerschrader.skillwill.domain.skills;

import org.springframework.data.annotation.Id;

/**
 * A skill owned by a person
 * includes name, skill level and will level
 *
 * @author torree
 */
public class UserSkill {

  @Id
  private String name;

  private int skillLevel;

  private int willLevel;

  private boolean mentor;

  public UserSkill(String name, int skillLevel, int willLevel, boolean mentor) {
    this.name = name;
    this.skillLevel = skillLevel;
    this.willLevel = willLevel;
    this.mentor = mentor;
  }

  public String getName() {
    return this.name;
  }

  public int getSkillLevel() {
    return skillLevel;
  }

  public void setSkillLevel(int skillLevel) {
    this.skillLevel = skillLevel;
  }

  public int getWillLevel() {
    return willLevel;
  }

  public void setWillLevel(int willLevel) {
    this.willLevel = willLevel;
  }

  public void setMentor(boolean mentor) {
    this.mentor = mentor;
  }

  public boolean isMentor() {
    return this.mentor;
  }

}
