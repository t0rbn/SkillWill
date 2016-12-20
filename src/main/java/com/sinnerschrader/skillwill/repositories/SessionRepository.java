package com.sinnerschrader.skillwill.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sinnerschrader.skillwill.session.Session;

/**
 * Repository for Sessions
 * Collection: session
 *
 * @author torree
 *
 */
public interface SessionRepository extends MongoRepository<Session, String> {

	public Session findByKey(String key);

	public Session findByUsername(String username);

}