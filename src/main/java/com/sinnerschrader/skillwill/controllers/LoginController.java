package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.exceptions.CredentialsException;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.services.LoginService;
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
@Api(tags = "Login", description = "Handles user createSession and logout")
@Controller
@CrossOrigin
@Scope("prototype")
public class LoginController {

  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

  @Autowired
  private LoginService loginService;

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
      @ApiResponse(code = 401, message = "Unauthorized"),
      @ApiResponse(code = 500, message = "Failure")
  })
  @RequestMapping(path = "/login", method = RequestMethod.POST)
  public ResponseEntity<String> login(@RequestParam("username") String username,
      @RequestParam("password") String password) {

    try {
      JSONObject obj = new JSONObject();
      obj.put("session", loginService.login(username, password));
      return new ResponseEntity<>(obj.toString(), HttpStatus.OK);
    } catch (CredentialsException e) {
      return new ResponseEntity<>(new StatusJSON("invalid credentials").toString(), HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * User Logout
   */
  @ApiOperation(value = "logout", nickname = "logout")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "session", value = "User's session key", required = true, paramType = "form")
  })
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request"),
      @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/logout", method = RequestMethod.POST)
  public ResponseEntity<String> logout(@RequestParam("session") String session) {
    try {
      loginService.logout(session);
      logger.info("Successfully logged out {}", session);
      return new ResponseEntity<>(new StatusJSON("logout successful").toString(), HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(new StatusJSON("session key not found").toString(), HttpStatus.BAD_REQUEST);
    }
  }

}