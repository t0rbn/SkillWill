package com.sinnerschrader.skillwill.misc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * I'm not a coffe maker!
 *
 * @author torree
 */
@Api(hidden = true)
@Controller
@Scope("prototype")
public class HTCPCPImpl {

	@ApiOperation(value = "HTCPCP Implementation", notes = "HTCPCP", hidden = true)
	@CrossOrigin("http://localhost:8888")
	@RequestMapping(path = "/coffee", method = RequestMethod.GET)
	public ResponseEntity<String> coffee() {
		StatusJSON json = new StatusJSON("I'm a teapot \u2615");
		return new ResponseEntity<String>(json.toString(), HttpStatus.I_AM_A_TEAPOT);
	}

}