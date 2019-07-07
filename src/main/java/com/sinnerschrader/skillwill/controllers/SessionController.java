package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.misc.OAuthHelper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "Session")
@Controller
@CrossOrigin
@Scope("prototype")
public class SessionController {

  private final OAuthHelper oAuthHelper;

  @Autowired
  public SessionController(OAuthHelper oAuthHelper) {
    this.oAuthHelper = oAuthHelper;
  }

  @ApiOperation(value = "session/user", nickname = "create session user", notes = "create session user")
  @RequestMapping(path = "/session/user", method = RequestMethod.GET)
  public ResponseEntity getCurrentUser(OAuth2AuthenticationToken oauthToken) {
    return new ResponseEntity<>(oAuthHelper.getUserFromToken(oauthToken), HttpStatus.OK);
  }

}
