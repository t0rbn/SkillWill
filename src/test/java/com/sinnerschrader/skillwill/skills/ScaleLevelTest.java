package com.sinnerschrader.skillwill.skills;

import org.junit.Test;

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
