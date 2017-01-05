package com.sinnerschrader.skillwill.repositories;

import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Repository for skills
 * Collection: knownSkill
 *
 * @author torree
 */
public interface SkillsRepository extends MongoRepository<KnownSkill, String> {

	KnownSkill findByName(String name);

	@Query("{ _id : {'$regex': ?0, $options: 'i'} }")
	List<KnownSkill> findFuzzyByName(String name);

	@Query("{ suggestions.name : ?0 }")
	List<KnownSkill> findBySuggestion(String suggestion);

}