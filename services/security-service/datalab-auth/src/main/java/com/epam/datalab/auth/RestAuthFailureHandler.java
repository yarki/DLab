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
package com.epam.datalab.auth;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.auth.UnauthorizedHandler;

public class RestAuthFailureHandler implements UnauthorizedHandler {
	
	private final static Logger LOG = LoggerFactory.getLogger(RestAuthFailureHandler.class);

	private final AuthenticationService authenticationService;
	
	public RestAuthFailureHandler(AuthenticationService as) {
		this.authenticationService = as;
	}
	
	@Override
	public Response buildResponse(String prefix, String realm) {
		String redirect = authenticationService.getLoginUrl();
		LOG.debug("Authentication failure redirect to {}",redirect);
		return Response.seeOther(URI.create(redirect)).build();
	}

}
