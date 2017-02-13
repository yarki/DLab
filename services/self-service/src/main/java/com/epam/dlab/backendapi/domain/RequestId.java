package com.epam.dlab.backendapi.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.backendapi.resources.KeyUploaderResource;
import com.epam.dlab.exceptions.DlabException;

public class RequestId {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUploaderResource.class);

	private static final Map<String, String> uuids = new HashMap<String, String>();
	
	public static String put(String username, String uuid) {
		LOGGER.debug("Register request id {} for user {}", uuid, username);
		uuids.put(uuid, username);
		return uuid;
	}
	
	public static String get(String username) {
		String uuid = UUID.randomUUID().toString();
		LOGGER.debug("Register request id {} for user {}", uuid, username);
		uuids.put(uuid, username);
		return uuid;
	}
	
	public static void remove(String uuid) throws DlabException {
		String username = RequestId.get(uuid);
		LOGGER.debug("Unregister request id {} for user {}", uuid, username);
		uuids.remove(uuid);
	}

	public static String checkAndRemove(String uuid) throws DlabException {
		String username = uuids.get(uuid);
		if (username == null) {
			throw new DlabException("Unknown request id " + uuid);
		}
		LOGGER.debug("Unregister request id {} for user {}", uuid, username);
		uuids.remove(uuid);
		return username;
	}
}
