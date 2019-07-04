package com.sinnerschrader.skillwill.domain.skills;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;

public class SkillSearchResult {

  private final Map<String, Skill> mapped;

  Set<String> unmapped;

  public SkillSearchResult(Map<String, Skill> mapped, Set<String> unmapped) {
    this.mapped = mapped;
    this.unmapped = unmapped;
  }

  public Map<String, Skill> getMapped() {
    return mapped;
  }

  public Set<Skill> mappedSkills() {
    return Set.copyOf(mapped.values());
  }

  public JSONArray mappingJson() {
    var array = new JSONArray();
    mapped.forEach((key, value) -> {
      var mappingObject = new JSONObject();
      mappingObject.put("input", key);
      mappingObject.put("found", value.getName());
      array.put(mappingObject);
    });
    return array;
  }

  public boolean isInputEmpty() {
    return CollectionUtils.isEmpty(mapped) && CollectionUtils.isEmpty(unmapped);
  }


}
