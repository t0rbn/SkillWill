package com.sinnerschrader.skillwill.misc;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class StatusJSON {

	private JSONObject json;

	public StatusJSON(String message, HttpStatus status) {
		JSONObject obj = new JSONObject();
		obj.put("message", message);
		obj.put("httpStatus", status.value());
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
