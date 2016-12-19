package com.sinnerschrader.skillwill.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sinnerschrader.skillwill.misc.StatusJSON;

/**
 * I'm not a coffe maker!
 * 
 * @author torree
 *
 */
@Controller
public class CoffeeController {

	@CrossOrigin("http://localhost:8888")
	@RequestMapping("/coffee")
	public ResponseEntity<String> coffee() {
		StatusJSON json = new StatusJSON("I'm a teapot \u2615", HttpStatus.I_AM_A_TEAPOT);
		return new ResponseEntity<String>(json.toString(), HttpStatus.I_AM_A_TEAPOT);
	}

}