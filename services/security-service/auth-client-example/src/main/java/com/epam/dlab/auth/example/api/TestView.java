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
package com.epam.dlab.auth.example.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.core.AuthenticationServiceConfig;
import com.epam.dlab.auth.core.UserInfo;

import io.dropwizard.views.View;

public class TestView extends View {
	
	private final static Logger LOG = LoggerFactory.getLogger(TestView.class);
	
	private final UserInfo user ;
	private final String accessToken;
	private final AuthenticationServiceConfig authenticationService;
	
	public TestView(UserInfo user, String token,AuthenticationServiceConfig as) {
		super("testauth.mustache");
		this.user = user;
		this.accessToken = token;
		this.authenticationService = as;
	}
	
	public String getName() {
		return user.getName();
	}
	
	public String getLogoutUrl() {
		return authenticationService.getLogoutUrl();
	}
	
	public String getAccessToken() {
		return accessToken;
	}
}
