package com.sinnerschrader.skillwill.skills;

import java.util.Comparator;

public class KnownSkillSuggestionComparator implements Comparator<KnownSkill> {

	private String userinput;

	public KnownSkillSuggestionComparator(String userinput) {
		this.userinput = userinput;
	}

	@Override
	public int compare(KnownSkill a, KnownSkill b) {
		boolean aStartsWith = a.getName().toLowerCase().startsWith(userinput.toLowerCase());
		boolean bStartsWith = b.getName().toLowerCase().startsWith(userinput.toLowerCase());

		return (aStartsWith ? -1 : 0) + (bStartsWith ? 1 : 0);
	}

}
