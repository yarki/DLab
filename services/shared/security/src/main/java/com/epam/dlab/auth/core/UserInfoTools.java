package com.epam.dlab.auth.core;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserInfoTools {

	private final static Logger LOG = LoggerFactory.getLogger(UserInfoTools.class);

	private final static ObjectMapper om = new ObjectMapper();

	public static String toJson(UserInfo ui) {
		String json;
		try {
			json = om.writeValueAsString(ui);
			LOG.debug("UserInfo to JSON {} -> {}", ui, json);
		} catch (JsonProcessingException e) {
			LOG.error("UserInfo to JSON failed " + ui, e);
			throw new RuntimeException(e);
		}
		return json;
	}

	public static UserInfo toUserInfo(String json) {
		UserInfo ui;
		try {
			ui = om.readerFor(UserInfo.class).readValue(json);
			LOG.debug("JSON to UserInfo {} -> {}", json, ui);
		} catch (IOException e) {
			LOG.error("JSON to UserInfo failed " + json, e);
			throw new RuntimeException(e);
		}
		return ui;
	}
}
