package com.sinnerschrader.skillwill.skills;

/**
 * A suggestable skill used by KnownSkill
 * 
 * @author torree
 *
 */
public class SuggestionSkill {

	private String name;
	private int count;

	public SuggestionSkill(String name) {
		this.name = name;
		this.count = 0;
	}

	public SuggestionSkill() {
		this.name = null;
		this.count = 0;
	}

	public SuggestionSkill(String name, int count) {
		this.name = name;
		this.count = count;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void incrementCount() {
		this.count += 1;
	}

}
