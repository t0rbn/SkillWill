package com.sinnerschrader.skillwill.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sinnerschrader.skillwill.person.Person;

/**
 * MongoRepository for Persons
 * Collection: person
 *
 * @author torree
 */
public interface PersonRepository extends MongoRepository<Person, String> {
    public Person findById(String id);

    @Query("{ skills._id : ?0 }")
    public List<Person> findBySkill(String skillName);

}