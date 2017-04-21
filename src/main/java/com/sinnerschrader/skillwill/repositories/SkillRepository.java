package com.sinnerschrader.skillwill.repositories;

import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Repository for skills
 * Collection: knownSkill
 *
 * @author torree
 */
public interface SkillRepository extends MongoRepository<KnownSkill, String> {

  KnownSkill findByName(String name);

  @Query("{ 'suggestions.name' : '?0' }")
  List<KnownSkill> findBySuggestion(String suggestion);

  KnownSkill findByNameStem(String name);

  List<KnownSkill> findByNameStemLike(String name);

}