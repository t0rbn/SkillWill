package com.sinnerschrader.skillwill.mock;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class MockData {

	public final JSONObject alice;
	public final JSONObject bob;
	public final JSONObject foobar;
	public final JSONArray allUsers;
	public final JSONArray someUsers;

	public MockData() {
		this.alice = new User("alikos", "Alice", "Koslowsky", "alice.koslowsky@sinnerschrader.com", "Hamburg", true,
				new PersonalSkill("Java", 1, 3), new PersonalSkill("JS", 3, 3), new PersonalSkill("Ruby", 4, 1)).toJSON();
		this.bob = new User("boband", "Bob", "Andrews", "bob.andrews@sinnerschrader.com", "Hamburg", true,
				new PersonalSkill("Java", 2, 2)).toJSON();
		this.foobar = new User("foobar", "Fooberius", "Bar", "fooberius.bar@sinnerschrader.com", "Frankurt", true,
				new PersonalSkill("Ruby", 1, 1), new PersonalSkill("Buchhaltung", 3, 2),
				new PersonalSkill("Photoshop", 4, 4), new PersonalSkill("Sketch", 1, 3)).toJSON();

		this.allUsers = new JSONArray();
		this.allUsers.put(alice);
		this.allUsers.put(bob);
		this.allUsers.put(foobar);

		this.someUsers = new JSONArray();
		this.someUsers.put(alice);
		this.someUsers.put(foobar);

	}

	private class PersonalSkill {

		String name;
		int skillLevel;
		int willLevel;

		public PersonalSkill(String name, int skillLevel, int willLevel) {
			this.name = name;
			this.skillLevel = skillLevel;
			this.willLevel = willLevel;
		}

		JSONObject toJSON() {
			JSONObject o = new JSONObject();
			o.put("name", this.name);
			o.put("skillLevel", this.skillLevel);
			o.put("willLevel", this.willLevel);
			return o;
		}

	}

	private class User {

		String id;
		String firstName;
		String lastName;
		String eMail;
		String location;
		boolean active;
		List<PersonalSkill> skills;

		public User(String id, String firstName, String lastName, String eMail, String Location, boolean active,
				PersonalSkill... skills) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
			this.eMail = eMail;
			this.active = active;

			this.skills = new LinkedList<PersonalSkill>();
			for (PersonalSkill s : skills) {
				this.skills.add(s);
			}
		}

		JSONObject toJSON() {
			JSONObject o = new JSONObject();
			o.put("id", this.id);
			o.put("firstName", this.firstName);
			o.put("lastName", this.lastName);
			o.put("eMail", this.eMail);
			o.put("location", this.location);
			o.put("active", this.active);
			o.put("skills", this.skills.stream().map(s -> s.toJSON()).collect(Collectors.toList()));
			return o;
		}

	}

}
