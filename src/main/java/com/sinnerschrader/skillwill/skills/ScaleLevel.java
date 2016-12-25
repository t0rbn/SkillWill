package com.sinnerschrader.skillwill.skills;

/**
 * Scale Level => unit of skill level/will level
 *
 * @author torree
 */
public class ScaleLevel {

    private int value;

    public ScaleLevel(int value) {
        if (0 <= value && value <= 3) {
            this.value = value;
        } else {
            throw new IllegalArgumentException("Value must be in scale 0-3");
        }
    }

    public int getInt() {
        return this.value;
    }

}
