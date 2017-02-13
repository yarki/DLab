package com.epam.dlab.backendapi.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.epam.dlab.exceptions.DlabException;

public class RequestId {
	
	private static final Map<String, String> uuids = new HashMap<String, String>();
	
	public static String put(String username, String uuid) {
		uuids.put(uuid, username);
		return uuid;
	}
	
	public static String get(String username) {
		String uuid = UUID.randomUUID().toString();
		uuids.put(uuid, username);
		return uuid;
	}
	
	public static void remove(String uuid) throws DlabException {
		
		uuids.remove(uuid);
	}

	public static String checkAndRemove(String uuid) throws DlabException {
		String username = uuids.get(uuid);
		if (username == null) {
			throw new DlabException("Unknown request id " + uuid);
		}
		uuids.remove(uuid);
		return username;
	}

	public static void main(String[] args) {
		String s = RequestId.get("user");
		RequestId.checkAndRemove(s);
		RequestId.remove(null);
		RequestId.checkAndRemove("111");
	}
}
