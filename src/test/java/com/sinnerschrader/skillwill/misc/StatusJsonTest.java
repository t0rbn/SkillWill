package com.sinnerschrader.skillwill.misc;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit tests for StatusJson
 *
 * @author torree
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class StatusJsonTest {

	@Test
	public void testNormalStrings() throws JSONException {
		StatusJSON json = new StatusJSON("foo", HttpStatus.I_AM_A_TEAPOT);
		assertEquals("foo", json.getJSON().get("message"));
		assertEquals(418, json.getJSON().get("httpStatus"));
	}

	@Test
	public void testEmptyStrings() throws JSONException {
		StatusJSON json = new StatusJSON("", HttpStatus.I_AM_A_TEAPOT);
		assertEquals("", json.getJSON().get("message"));
		assertEquals(418, json.getJSON().get("httpStatus"));
	}

}
