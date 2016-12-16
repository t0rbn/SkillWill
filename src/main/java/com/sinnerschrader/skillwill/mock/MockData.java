package com.sinnerschrader.skillwill.mock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.skills.KnownSkill;
import com.sinnerschrader.skillwill.skills.PersonalSkill;

@Component
public class MockData {

	@Autowired
	SkillsRepository skillRepo;

	@Autowired
	PersonRepository personRepo;

	@PostConstruct
	public void fillData() {
		skillRepo.deleteAll();
		skillRepo.insert(new KnownSkill("Java"));
		skillRepo.insert(new KnownSkill("JavaScript"));
		skillRepo.insert(new KnownSkill("COBOL"));
		skillRepo.insert(new KnownSkill("CSS"));
		skillRepo.insert(new KnownSkill("Sketch"));
		skillRepo.insert(new KnownSkill("AEM"));
		skillRepo.insert(new KnownSkill("TypeScript"));
		skillRepo.insert(new KnownSkill("Scrum"));

		personRepo.deleteAll();

		Person a = new Person("alikow", "Alice", "Kowalsky");
		a.addUpdateSkill(new PersonalSkill("Java",       0, 1));
		a.addUpdateSkill(new PersonalSkill("JavaScript", 1, 2));
		a.addUpdateSkill(new PersonalSkill("TypeScript", 2, 1));
		a.addUpdateSkill(new PersonalSkill("AEM",        3, 0));
		a.addUpdateSkill(new PersonalSkill("COBOL",      2, 2));

		Person b = new Person("boband", "Bob", "Andrews");
		b.addUpdateSkill(new PersonalSkill("Sketch",     2, 1));
		b.addUpdateSkill(new PersonalSkill("Scrum",      2, 3));


		Person c = new Person("chacha", "Charlie", "Chaplin");
		c.addUpdateSkill(new PersonalSkill("Java",       1, 3));
		c.addUpdateSkill(new PersonalSkill("COBOL",      2, 2));
		c.addUpdateSkill(new PersonalSkill("Scrum",      3, 1));

		personRepo.insert(a);
		personRepo.insert(b);
		personRepo.insert(c);
	}
}
