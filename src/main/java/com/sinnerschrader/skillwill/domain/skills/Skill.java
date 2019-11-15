package com.sinnerschrader.skillwill.domain.skills;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

public class Skill {

  private String name;

  @Id
  private String nameStem;

  private List<SuggestionSkill> suggestions;


  @Version
  private Long version;

  public Skill(String name, List<SuggestionSkill> suggestions) {
    this.name = name;
    this.nameStem = SkillUtils.toStem(name);
    this.suggestions = suggestions;
  }

  public Skill(String name) {
    this(name, new ArrayList<>());
  }

  public Skill() {
    this("", new ArrayList<>());
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
    this.nameStem = SkillUtils.toStem(name);
  }

  public List<SuggestionSkill> getSuggestions() {
    return this.suggestions;
  }

  public void setSuggestions(List<SuggestionSkill> suggestions) {
    this.suggestions = suggestions;
  }

  private SuggestionSkill getSuggestionByName(String name) {
    return this.suggestions.stream()
      .filter(s -> s.getName().equals(name))
      .findFirst()
      .orElse(null);
  }

  public void renameSuggestion(String oldName, String newName) {
    SuggestionSkill suggestion = getSuggestionByName(oldName);

    if (suggestion == null) {
      // no suggestion to rename
      return;
    }

    suggestion.setName(newName);
  }

  public void incrementSuggestion(String name) {
    SuggestionSkill suggestion = getSuggestionByName(name);

    if (suggestion != null) {
      suggestion.incrementCount();
    } else {
      suggestions.add(new SuggestionSkill(name, 1));
    }
  }

  public void deleteSuggestion(String name) {
    SuggestionSkill suggestion = getSuggestionByName(name);
    if (suggestion == null) {
      // no suggestion to rename
      return;
    }
    this.suggestions.remove(suggestion);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Skill skill = (Skill) o;
    return Objects.equals(name, skill.name) &&
      Objects.equals(nameStem, skill.nameStem) &&
      Objects.equals(suggestions, skill.suggestions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, nameStem, suggestions);
  }

}
