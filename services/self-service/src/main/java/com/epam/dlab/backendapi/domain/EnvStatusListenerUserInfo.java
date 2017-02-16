package com.epam.dlab.backendapi.domain;

import com.epam.dlab.dto.status.EnvResourceDTO;
import com.epam.dlab.utils.UsernameUtils;

class EnvStatusListenerUserInfo {
	/** Time for the next check in milliseconds. */
    long nextCheckTimeMillis;
    /** Name of user. */
    String username;
    /** Access token for request provisioning service. */
    String accessToken;
    
    EnvResourceDTO dto;

    public EnvStatusListenerUserInfo(String username, String accessToken, String awsRegion) {
		this.nextCheckTimeMillis = System.currentTimeMillis();
    	this.accessToken = accessToken;
    	this.username = username;
    	dto = new EnvResourceDTO()
    			.withAwsRegion(awsRegion)
    			.withEdgeUserName(UsernameUtils.removeDomain(username))
    			.withIamUserName(username);
	}

    public long getNextCheckTimeMillis() {
    	return nextCheckTimeMillis;
    }
    
    public String getUsername() {
    	return username;
    }
    
    public String getAccessToken() {
    	return accessToken;
    }
    
    public EnvResourceDTO getDTO() {
    	return dto;
    }

	public void setNextCheckTimeMillis(long nextCheckTimeMillis) {
		this.nextCheckTimeMillis = nextCheckTimeMillis;
	}
}