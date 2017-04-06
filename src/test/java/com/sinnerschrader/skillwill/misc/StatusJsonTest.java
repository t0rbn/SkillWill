package com.sinnerschrader.skillwill.misc;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit tests for StatusJson
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class StatusJsonTest {

  @Test
  public void testNormalStrings() throws JSONException {
    StatusJSON json = new StatusJSON("foo");
    assertEquals("foo", json.getJSON().get("message"));
  }

  @Test
  public void testEmptyStrings() throws JSONException {
    StatusJSON json = new StatusJSON("");
    assertEquals("", json.getJSON().get("message"));
  }

  @Test
  public void testUnicodeMessage() throws JSONException {
    StatusJSON json = new StatusJSON("\uD83D\uDCA9");
    assertEquals("\uD83D\uDCA9", json.getJSON().get("message"));
  }
}
