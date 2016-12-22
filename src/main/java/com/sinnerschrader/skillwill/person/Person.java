package com.sinnerschrader.skillwill.person;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import com.sinnerschrader.skillwill.ldap.LdapSync;
import com.sinnerschrader.skillwill.ldap.PersonalLDAPDetails;
import com.sinnerschrader.skillwill.skills.PersonalSkill;

/**
 * Class holding all information about a person
 *
 * @author torree
 *
 */
public class Person {

	@Id
	private String id;
	private List<PersonalSkill> skills;

	// LDAP Details will be updates regularly
	private PersonalLDAPDetails ldapDetails;

	public Person(String id) {
		this.id = id;
		this.skills = new ArrayList<PersonalSkill>();
		this.ldapDetails = null;
	}

	public String getId() {
		return this.id;
	}

	public List<PersonalSkill> getSkills() {
		return this.skills;
	}

	public void setLdapDetails(PersonalLDAPDetails ldapDetails) {
		this.ldapDetails = ldapDetails;
	}

	public PersonalLDAPDetails getLdapDetails() {
		return this.ldapDetails;
	}

	public void addUpdateSkill(PersonalSkill skill) {
		// Remove old skill if existing...
		this.skills = skills.stream()
				.filter(s -> !s.getName().equals(skill.getName()))
				.collect(Collectors.toList());

		// ...insert new one with updates/new values
		this.skills.add(skill);
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("id", this.id);

		if (this.ldapDetails != null) {
			obj.put("firstName", ldapDetails.getFirstName());
			obj.put("lastName", ldapDetails.getLastName());
			obj.put("mail", ldapDetails.getMail());
			obj.put("phone", ldapDetails.getPhone());
			obj.put("location", ldapDetails.getLocation());
			obj.put("title", ldapDetails.getTitle());

		}

		JSONArray skills = new JSONArray();
		for (PersonalSkill s : this.skills) {
			skills.put(s.toJSON());
		}

		obj.put("skills", skills);
		return obj;
	}

}
