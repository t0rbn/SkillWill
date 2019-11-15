package com.sinnerschrader.skillwill.domain.skills;

import java.util.Comparator;

public class SkillAutocompleteComparator implements Comparator<Skill> {

  private final String userinput;

  public SkillAutocompleteComparator(String userinput) {
    this.userinput = userinput;
  }

  @Override
  public int compare(Skill a, Skill b) {
    boolean aStartsWith = a.getName().toLowerCase().startsWith(userinput.toLowerCase());
    boolean bStartsWith = b.getName().toLowerCase().startsWith(userinput.toLowerCase());

    return (aStartsWith ? -1 : 0) + (bStartsWith ? 1 : 0);
  }

}
