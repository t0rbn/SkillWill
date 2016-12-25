package com.sinnerschrader.skillwill.misc;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiOperation;

/**
 * Forward / to /swagger-ui.html
 *
 * @author torree
 */
@Controller
@Scope("prototype")
public class SwaggerForward {

    @ApiOperation(value = "redirect to swagger", notes = "redirect to swagger", hidden = true)
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String redirectRoot() {
        return "redirect:/swagger-ui.html";
    }

}
