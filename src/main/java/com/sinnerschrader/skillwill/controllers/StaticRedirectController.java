package com.sinnerschrader.skillwill.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Scope("prototype")
public class StaticRedirectController {

//  @ApiOperation(value = "forward to swagger", notes = "forward to swagger")
  @RequestMapping(path = "/swagger")
  public String forwardSwagger() {
    return "redirect:/swagger-ui.html";
  }

//  @ApiOperation(value = "forward to swagger", notes = "forward to swagger")
  @RequestMapping(path = "/admin")
  public String forwardAdmin() {
    return "redirect:/admin.html";
  }

//  @ApiOperation(value = "forward frontend to index", notes = "forward routes handled by react-router to index")
  @RequestMapping(path = {"/my-profile", "/profile/{user}"})
  public String forwardIndex() {
    return "redirect:/";
  }

}
