package com.sinnerschrader.skillwill.person;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;

import com.sinnerschrader.skillwill.skills.PersonalSkill;

public class Person {

	@Id
	private String id;
	private String firstName;
	private String lastName;
	private List<PersonalSkill> skills;

	public Person(String id, String firstName, String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.skills = new ArrayList<PersonalSkill>();
	}

	public String getId() {
		return this.id;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public List<PersonalSkill> getSkills() {
		return this.skills;
	}

	public void addUpdateSkill(PersonalSkill skill) {
		for (PersonalSkill old : this.skills) {
			if (old.getName().equals(skill.getName())) {
				skills.remove(old);
			}
		}
		this.skills.add(skill);
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("id", this.id);
		obj.put("firstName", this.firstName);
		obj.put("lastName", this.lastName);

		JSONArray skills = new JSONArray();
		for (PersonalSkill s : this.skills) {
			skills.put(s.toJSON());
		}

		obj.put("skills", skills);
		return obj;
	}

}
