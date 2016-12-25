package com.sinnerschrader.skillwill.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.Id;

/**
 * A skill known to the system including a list of suggestable skills
 *
 * @author torree
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

    private SuggestionSkill getSuggestionByName(String name) {
        Optional<SuggestionSkill> found = this.suggestions.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
        return found.isPresent() ? found.get() : null;
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

}
