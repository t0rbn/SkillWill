package com.sinnerschrader.skillwill.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sinnerschrader.skillwill.person.Person;

public interface PersonRepository extends MongoRepository<Person, String> {
	public Person findById(String id);

	@Query("{ skills.name : ?0 }")
	public List<Person> findBySkillName(String skillName);

	@Query("{ skills.name : ?0 }")
	public List<Person> findBySkillNames(List<String> names);

}