package com.sinnerschrader.skillwill.repositories;

import com.sinnerschrader.skillwill.session.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for Sessions
 * Collection: session
 *
 * @author torree
 */
public interface SessionRepository extends MongoRepository<Session, String> {

	Session findByKey(String key);

	Session findByUsername(String username);

}