package com.sinnerschrader.skillwill.skills;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

/**
 * A skill known to the system including a list of suggestable skills

 * @author torree
 *
 */
public class KnownSkill {

	@Id
	private String name;
	private List<SuggestionSkill> suggestions;

	public KnownSkill(String name) {
		this.name = name;
		this.suggestions = new ArrayList<SuggestionSkill>();
	}

	public KnownSkill(String name, List<SuggestionSkill> suggestions) {
		this.name = name;
		this.suggestions = suggestions;
	}

	public KnownSkill() {
		this.name = null;
		this.suggestions = null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public List<SuggestionSkill> getSuggestions() {
		return this.suggestions;
	}

	public void setSuggestions(List<SuggestionSkill> suggestions) {
		this.suggestions = suggestions;
	}

	public void renameSuggestions(String oldName, String newName) {
		for (SuggestionSkill s : this.suggestions) {
			if (s.getName().equals(oldName)) {
				this.suggestions.remove(s);
				this.suggestions.add(new SuggestionSkill(newName, s.getCount()));
			}
		}
	}

	public void incrementSuggestion(String name) {
		for (SuggestionSkill s : suggestions) {
			if (s.getName().equals(name)) {
				s.incrementCount();
				return;
			}
		}

		SuggestionSkill newSkill = new SuggestionSkill(name, 1);
		suggestions.add(newSkill);
	}

	public void deleteSuggestion(String name) {
		for (SuggestionSkill s : suggestions) {
			if (s.getName().equals(name)) {
				suggestions.remove(s);
			}
		}
	}

}
