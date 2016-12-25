package com.sinnerschrader.skillwill.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Integration test for the implementation
 * of the Hyper Text Coffee Pot Control Protocol
 *
 * @author torree
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class HTCPCPTest {

	@Autowired
	private HTCPCPImpl coffeeMaker;

	@Test
	public void testHTCPCP() {
		assertEquals(HttpStatus.I_AM_A_TEAPOT, coffeeMaker.coffee().getStatusCode());
	}

}
