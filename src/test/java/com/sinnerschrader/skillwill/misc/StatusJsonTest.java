package com.sinnerschrader.skillwill.misc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class StatusJsonTest {

	@Test
	public void testNormalStrings() {
		StatusJSON json = new StatusJSON("foo", HttpStatus.I_AM_A_TEAPOT);
		assertEquals("foo", json.getJSON().get("message"));
		assertEquals(418, json.getJSON().get("httpStatus"));
	}

	@Test
	public void testEmptyStrings() {
		StatusJSON json = new StatusJSON("", HttpStatus.I_AM_A_TEAPOT);
		assertEquals("", json.getJSON().get("message"));
		assertEquals(418, json.getJSON().get("httpStatus"));
	}

}
