package com.sinnerschrader.skillwill.repositories;

import com.sinnerschrader.skillwill.domain.person.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * MongoRepository for Persons
 * Collection: person
 *
 * @author torree
 */
public interface PersonRepository extends MongoRepository<Person, String> {
	Person findById(String id);

	@Query("{ 'skills._id' : '?0' }")
	List<Person> findBySkill(String skillName);

	@Query("{ 'skills._id' : { $all : ?0 } }")
	List<Person> findBySkills(List<String> skillNames);

}