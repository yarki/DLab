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

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.core.AuthenticationServiceConfig;
import com.epam.dlab.auth.core.UserInfo;
import com.google.inject.Inject;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

public class RestAuthenticator implements Authenticator<String, UserInfo> {

	private final static Logger LOG = LoggerFactory.getLogger(RestAuthenticator.class);

	private final AuthenticationServiceConfig authenticationService;
	private final Client client;

	@Inject
	public RestAuthenticator(AuthenticationServiceConfig as, Client client) {
		this.authenticationService = as;
		this.client = client;
	}

	@Override
	public Optional<UserInfo> authenticate(String credentials) throws AuthenticationException {
		LOG.debug("Authenticate token {}", credentials);
		WebTarget target = client.target(authenticationService.getUserInfoUrl()).queryParam("access_token",credentials);
		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		UserInfo bean = target.request(MediaType.APPLICATION_JSON_TYPE).get(UserInfo.class);
		Optional<UserInfo> result = Optional.ofNullable(bean);
		result.orElseThrow(() -> new WebApplicationException(
				Response.seeOther(URI.create(authenticationService.getLoginUrl())).build()));
		return result;
	}

}
