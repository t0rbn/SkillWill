package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.exceptions.CredentialsException;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.services.LoginService;
import com.sinnerschrader.skillwill.services.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller Handling /user
 *
 * @author torree
 */
@Api(tags = "Login", description = "Handles user create and remove")
@Controller
@CrossOrigin
@Scope("prototype")
public class LoginController {

  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

  @Autowired
  private LoginService loginService;

  @Autowired
  private SessionService sessionService;

  /**
   * User Login
   */
  @ApiOperation(value = "login", nickname = "login")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "username", value = "User's ID", required = true, paramType = "form"),
      @ApiImplicitParam(name = "password", value = "User's Password", required = true, paramType = "form")
  })
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 500, message = "Failure")
  })
  @RequestMapping(path = "/login", method = RequestMethod.POST)
  public ResponseEntity<String> login(@RequestParam("username") String username,
      @RequestParam("password") String password) {

    try {
      JSONObject obj = new JSONObject();
      obj.put("sessionKey", loginService.login(username, password));
      return new ResponseEntity<>(obj.toString(), HttpStatus.OK);
    } catch (CredentialsException e) {
      return new ResponseEntity<>(new StatusJSON("invalid credentials").toString(), HttpStatus.FORBIDDEN);
    }
  }

  /**
   * User Logout
   */
  @ApiOperation(value = "remove", nickname = "remove")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "sessionKey", value = "User's session key", required = true, paramType = "form")
  })
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request"),
      @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/logout", method = RequestMethod.POST)
  public ResponseEntity<String> logout(@RequestParam String sessionKey) {
    try {
      loginService.logout(sessionKey);
      logger.info("Successfully logged out {}", sessionKey);
      return new ResponseEntity<>(new StatusJSON("remove successful").toString(), HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(new StatusJSON("session key not found").toString(), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Check sessionkey
   */
  @ApiOperation(value = "session checker", nickname = "session checker")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "username", value = "Username", required = true, paramType = "query"),
    @ApiImplicitParam(name = "sessionKey", value = "User's session key", required = true, paramType = "query")
  })
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/sessioncheck", method = RequestMethod.GET)
  public ResponseEntity<String> checkSession(@RequestParam String sessionKey, @RequestParam(value = "username") String username) {
    JSONObject json = new JSONObject();
    json.put("sessionKey", sessionKey);
    json.put("username", username);
    json.put("valid", sessionService.check(sessionKey, username));
    return new ResponseEntity<>(json.toString(), HttpStatus.OK);
  }

}