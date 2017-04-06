package com.sinnerschrader.skillwill.misc;

import org.json.JSONObject;

/**
 * Little helper class encapsulating status JSON
 * that will be returned in case of errors
 *
 * @author torree
 */
public class StatusJSON {

  private final JSONObject json;

  public StatusJSON(String message) {
    JSONObject obj = new JSONObject();
    obj.put("message", message);
    this.json = obj;
  }

  public JSONObject getJSON() {
    return this.json;
  }

  @Override
  public String toString() {
    return this.json.toString();
  }

}
