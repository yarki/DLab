package com.epam.dlab.backendapi.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.core.AuthenticationServiceConfig;
import com.epam.dlab.auth.core.UserInfo;

import io.dropwizard.views.View;

public class AfterLoginView extends View {

	private final static Logger LOG = LoggerFactory.getLogger(AfterLoginView.class);
	
	private final UserInfo user ;
	private final String accessToken;
	private final AuthenticationServiceConfig authenticationService;
	
	protected AfterLoginView(UserInfo user, String token, AuthenticationServiceConfig as) {
		super("afterlogin.mustache");
		this.user = user;
		this.accessToken = token;
		this.authenticationService = as;
	}

	public String getName() {
		return user.getName();
	}
	
	public String getLogoutUrl() {
		return "/logout?";
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	
}
