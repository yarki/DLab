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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.example.ExampleConfiguration;
import com.epam.dlab.auth.rest.ConfigurableResource;

import io.dropwizard.auth.Auth;

@Path("/{SUB}")
@Produces(MediaType.TEXT_HTML)
@RolesAllowed("TestService")
public class TestService extends ConfigurableResource<ExampleConfiguration>{
	public TestService(ExampleConfiguration config) {
		super(config);
	}

	@GET
	public TestView testView(@Auth UserInfo user, @QueryParam("access_token") String token) {
		return new TestView(user,token,config.getAuthenticationService());
	}
	

	
}
