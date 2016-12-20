package com.sinnerschrader.skillwill.controllers;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sinnerschrader.skillwill.ldap.LDAPLogin;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.session.SessionManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller Handling /user
 *
 * @author torree
 *
 */
@Api(tags = "Login", description = "Handles user login and logout")
@Controller
public class LoginController {

	private static Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private PersonRepository personRepo;

	@Autowired
	private SessionManager sessionManager;

	/**
	 *  User Login
	 */
	@ApiOperation(value = "login", nickname = "login")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "username", value = "User's ID", required = true, paramType = "form"),
		@ApiImplicitParam(name = "password", value = "User's Password", required = true, paramType = "form")
	})
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 500, message = "Failure")
	})
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/login", method = RequestMethod.POST)
	public ResponseEntity<String> login(@RequestParam("username") String username, @RequestParam("password") String password) {
		if (!LDAPLogin.canAuthenticate(username, password)) {
			logger.info("cannot " + username + " login: username/password incorrect");
			StatusJSON json = new StatusJSON("cannot login: username/password incorrect", HttpStatus.UNAUTHORIZED);
			return new ResponseEntity<String>(json.toString(), HttpStatus.UNAUTHORIZED);
		}

		// Insert User if not already exisitng
		// So a user does not need to create an account fist
		if (personRepo.findById(username) == null) {
			logger.info("Create new user " + username);
			personRepo.insert(new Person(username));
		}

		logger.info("Logging in " + username);

		JSONObject obj = new JSONObject();
		obj.put("session", sessionManager.login(username));
		return new ResponseEntity<String>(obj.toString(), HttpStatus.OK);
	}

	/**
	 * User Logout
	 */
	@ApiOperation(value = "logout", nickname = "logout")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "username", value = "User's ID", required = true, paramType = "form"),
		@ApiImplicitParam(name = "session", value = "User's session key", required = true, paramType = "form")
	})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 500, message = "Failure") })
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/logout", method = RequestMethod.POST)
	public ResponseEntity<String> logout(@RequestParam("username") String username, @RequestParam("session") String session) {
		try {
			sessionManager.logout(username, session);
		} catch (IllegalArgumentException e) {
			logger.info("error logging out " + username + " session key not found or username not matching");
			StatusJSON json = new StatusJSON("session key not found or username not matching", HttpStatus.BAD_REQUEST);
			return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
		}

		logger.info("logged out " + username);
		StatusJSON json = new StatusJSON("logout successful", HttpStatus.OK);
		return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
	}

}