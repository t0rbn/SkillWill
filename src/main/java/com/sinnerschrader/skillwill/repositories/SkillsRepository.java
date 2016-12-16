package com.sinnerschrader.skillwill.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sinnerschrader.skillwill.skills.KnownSkill;

public interface SkillsRepository extends MongoRepository<KnownSkill, String> {

	public KnownSkill findByName(String name);

	@Query("{ _id : {'$regex': ?0, $options: 'i'} }")
	public List<KnownSkill> findFuzzyByName(String name);

}