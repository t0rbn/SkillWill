package com.sinnerschrader.skillwill.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Login", description = "Handles user login and logout")
@Controller
public class LoginController {

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
	@RequestMapping(path = "/login", method = RequestMethod.POST)
	public ResponseEntity<String> login(@RequestParam("username") String username, @RequestParam("password") String password) {
		JSONObject returnStatus = new JSONObject();
		returnStatus.put("session", "b374d9b3b58269309bf67d5ba2879c3e");
		return new ResponseEntity<String>(returnStatus.toString(), HttpStatus.OK);
	}

	/**
	 * User Logout
	 */
	@ApiOperation(value = "logout", nickname = "logout")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 500, message = "Failure") })
	@RequestMapping(path = "/logout", method = RequestMethod.GET)
	public ResponseEntity<String> logout() {
		JSONObject returnStatus = new JSONObject();
		returnStatus.put("status", "success");
		return new ResponseEntity<String>(returnStatus.toString(), HttpStatus.OK);
	}

}