/*
Copyright 2016 EPAM Systems, Inc.
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.epam.dlab.auth.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationService {
	// http://username:password@example.com/
	@JsonProperty
	private String host = "localhost";
	@JsonProperty
	private int port = 0;
	@JsonProperty
	private String type = "http";
	@JsonProperty
	private String username = "";
	@JsonProperty
	private String password = "";
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(type == null || "".equals(type)) {
			sb.append("//");
		} else {
			sb.append(type).append("://");
		}
		if( username != null && ! "".equals(username)) {
			sb.append(username).append(":").append(password).append("@");
		}
		sb.append(host);
		if(port > 0) {
			sb.append(":").append(port);
		}
		return sb.toString();	}
	// http://username:password@example.com/
	
	public String getAuthenticationUrl() {
		return toString()+"/login?";
	}
	public String getAuthorizationUrl() {
		return toString()+"/authorize?";
	}
	public String getLogoutUrl() {
		return toString()+"/logout?";
	}
	public String getLoginUrl() {
		return toString()+"/?";
	}

}
