package com.sinnerschrader.skillwill.skills;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for SkillLevel
 *
 * @author torree
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ScaleLevelTest {

	@Test
	public void testInRange() {
		new ScaleLevel(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegative() {
		new ScaleLevel(-23);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTooHigh() {
		new ScaleLevel(42);		
	}

}
