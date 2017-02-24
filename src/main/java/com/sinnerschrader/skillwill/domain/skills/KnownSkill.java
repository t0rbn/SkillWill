package com.sinnerschrader.skillwill.domain.skills;

import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * A skill known to the system including a list of suggestable skills
 *
 * @author torree
 */
public class KnownSkill {

	@Id
	private String name;
	private String iconDescriptor;
	private List<SuggestionSkill> suggestions;

	@Version
	private Long version;

	public KnownSkill(String name, String iconDescriptor, List<SuggestionSkill> suggestions) {
		this.name = name;
		this.iconDescriptor = iconDescriptor;
		this.suggestions = suggestions;
	}

	public KnownSkill(String name, String iconDescriptor) {
		this(name, iconDescriptor, new ArrayList<>());
	}

	public KnownSkill() {
		this("", "", null);
	}


	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String getIconDescriptor() {
		return this.iconDescriptor;
	}

	public void setIconDescriptor(String iconDescriptor) {
		this.iconDescriptor = iconDescriptor;
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

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("name", this.name);
		obj.put("iconDescriptor", this.iconDescriptor);
		return obj;
	}

}
