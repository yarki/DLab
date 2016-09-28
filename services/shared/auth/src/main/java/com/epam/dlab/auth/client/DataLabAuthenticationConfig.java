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
package com.epam.dlab.auth.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.core.UserInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;


public class DataLabAuthenticationConfig extends Configuration {
	
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	@JsonProperty
	private long inactiveUserTimeoutMillSec = 600000;
	
	@NotNull
	private String defaultRedirectFromAuthentication;
	
	@JsonProperty
	private final List<Map<String,Object>> users = new ArrayList<>();
	
	public DataLabAuthenticationConfig() {
		super();
	}

	@JsonProperty
	public String getDefaultRedirectFromAuthentication() {
		LOG.debug("Requested DefaultRedirectFromAuthentication: {}",defaultRedirectFromAuthentication);
		return defaultRedirectFromAuthentication;
	}

	public void setDefaultRedirectFromAuthentication(String defaultRedirect) {
		this.defaultRedirectFromAuthentication = defaultRedirect;
	}
	
	public Map<String,UserInfo> getUsers() {
		Map<String,UserInfo> m = new ConcurrentHashMap<>();
		for(Map<String,Object> user:users) {
			UserInfo ui = new UserInfo(""+user.get("username"),""+user.get("password"));
			for(Object o:((Map<String,?>)user.get("roles")).keySet()){
				ui.addRole(o.toString());
			}
			m.put(ui.getName(),ui);
		}
		LOG.debug("Requested Users: {}",m);
		return m;
	}

	public long getInactiveUserTimeoutMillSec() {
		LOG.debug("Requested inactiveUserTimeoutMillSec: {}",inactiveUserTimeoutMillSec);
		return inactiveUserTimeoutMillSec;
	}
	
}
