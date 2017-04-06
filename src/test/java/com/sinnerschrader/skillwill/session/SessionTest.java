package com.sinnerschrader.skillwill.session;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Partial unit tests for Session
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SessionTest {

  private Session session;

  @Before
  public void setUp() {
    session = new Session("abc123", "foobar", new Date());
    session.renewSession(60);
  }

  @Test
  public void testIsExpiredUnexpired() {
    assertFalse(session.isExpired());
  }

  @Test
  public void testIsExpiredExpired() throws InterruptedException {
    session = new Session("abc123", "foobar", new Date());
    // Wait for session to expire
    Thread.sleep(50);
    assertTrue(session.isExpired());
  }

  @Test
  public void testRenewSession() throws InterruptedException {
    session = new Session("abc123", "foobar", new Date());
    // Wait for session to expire
    Thread.sleep(50);
    assertTrue(session.isExpired());
    session.renewSession(60);
    assertFalse(session.isExpired());

  }

}
