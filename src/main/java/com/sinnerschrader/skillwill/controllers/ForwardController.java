package com.sinnerschrader.skillwill.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Forward / to /swagger-ui.html
 *
 * @author torree
 */
@Controller
@Scope("prototype")
public class ForwardController {

  @ApiOperation(value = "forward to swagger", notes = "forward to swagger")
  @RequestMapping(path = "/swagger")
  public String redirectRoot() {
    return "redirect:/swagger-ui.html";
  }

  @ApiOperation(value = "forward frontend to index", notes = "forward routes handled by react-router to index")
  @RequestMapping(path = {"/my-profile/{user}", "/profile/{user}"})
  public String redirectFrontendIndex() {
    return "redirect:/";
  }

}
