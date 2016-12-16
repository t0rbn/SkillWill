package com.sinnerschrader.skillwill.skills;

public class ScaleLevel {

	private int value;

	public ScaleLevel(int value) {
		if (0 <= value && value <= 3) {
			this.value = value;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public int getInt() {
		return this.value;
	}

}
