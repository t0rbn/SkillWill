package com.sinnerschrader.skillwill.skills;

import org.json.JSONObject;
import org.springframework.data.annotation.Id;

/**
 * A skill owned by a person
 * includes name, skill level and will level
 *
 * @author torree
 */
public class PersonalSkill {

    @Id
    private String name;
    private ScaleLevel skillLevel;
    private ScaleLevel willLevel;

    public PersonalSkill(String name, int skillLevel, int willLevel) {
        this(name, new ScaleLevel(skillLevel), new ScaleLevel(willLevel));
    }

    public PersonalSkill(String name, ScaleLevel skillLevel, ScaleLevel willLevel) {
        this.name = name;
        this.skillLevel = skillLevel;
        this.willLevel = willLevel;
    }

    public PersonalSkill() {
        this.name = null;
        this.skillLevel = null;
        this.willLevel = null;
    }

    public String getName() {
        return this.name;
    }

    public ScaleLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(ScaleLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public ScaleLevel getWillLevel() {
        return willLevel;
    }

    public void setWillLevel(ScaleLevel willLevel) {
        this.willLevel = willLevel;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("name", this.name);
        o.put("skillLevel", this.skillLevel.getInt());
        o.put("willLevel", this.willLevel.getInt());
        return o;
    }

}
