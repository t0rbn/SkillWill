package com.sinnerschrader.skillwill.session;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.repositories.SessionRepository;

/**
 * Manage all sessions
 * NOTE: there can be multiple sessions
 * for one username
 * 
 * @author torree
 *
 */
@Component
@EnableScheduling
public class SessionManager {

	private static Logger logger = LoggerFactory.getLogger(SessionManager.class);

	// Minutes of inactivity before the session is destroyed
	@Value("${sessionExpireDuration}")
	private int expireDuration;

	@Autowired
	private SessionRepository sessionRepo;

	public String login(String username) {
		String key = null;
		do {
			key = UUID.randomUUID().toString().replaceAll("-", "");
		} while (key == null || sessionRepo.findByKey(key) != null);

		// Session is initialized with expire date = now
		// renew to set initial expiration date
		Session session = new Session(key, username, new Date());
		sessionRepo.insert(session);
		renewSession(session);

		logger.debug("Created new session for " + username);

		return session.getKey();
	}

	public boolean checkSession(String username, String sessionKey) {
		Session session = sessionRepo.findByKey(sessionKey);
		
		if (session == null) {
			logger.debug("Error checking session " + sessionKey + ": not found in DB");
			return false;
		}

		if (!session.getUsername().equals(username)) {
			logger.debug("Error checking session " + sessionKey + ": username does not match key");
			return false;
		}

		if (session.isExpired()) {
			logger.debug("Session " + sessionKey + " is expired, will remove from DB");
			sessionRepo.delete(session);
			return false;
		}

		renewSession(session);
		logger.debug("Successfully checked session " + sessionKey);

		return true;
	}

	public void logout(String username, String sessionKey) {
		Session session = sessionRepo.findByKey(sessionKey);

		if (session == null || !session.getUsername().equals(username)) {
			throw new IllegalArgumentException("session key not found or username not matching");
		}

		sessionRepo.delete(session);
	}

	private void renewSession(Session session) {
		logger.debug("renewing session " + session.getKey());
		session.renewSession(expireDuration);
		sessionRepo.save(session);
	}

	// Regularly clean up expired sessions that have been inactive
	// but not logged out properly
	@Scheduled(cron = "${sessionCleanUpCron}")
	public void cleanUp() {
		logger.info("Performing scheduled session cleanup");
		List<Session> expiredSessions = sessionRepo.findAll().stream()
				.filter(s -> s.isExpired())
				.collect(Collectors.toList());

		for (Session s : expiredSessions) {
			sessionRepo.delete(s);
		}
	}

}
