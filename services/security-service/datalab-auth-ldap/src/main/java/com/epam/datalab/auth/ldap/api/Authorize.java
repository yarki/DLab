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
package com.epam.datalab.auth.ldap.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.epam.datalab.auth.DataLabAuthenticationConfig;
import com.epam.datalab.auth.api.ConfigurableResource;
import com.epam.datalab.auth.core.AuthorizedUsers;
import com.epam.datalab.auth.core.UserInfo;

@Path("/authorize")
@Produces(MediaType.APPLICATION_JSON)
public class Authorize extends ConfigurableResource<DataLabAuthenticationConfig>{
	public Authorize(DataLabAuthenticationConfig config) {
		super(config);
	}

	@GET
	public UserInfo authenticate(@QueryParam("access_token") String token) {
		UserInfo ui = AuthorizedUsers.getInstance().getUserInfo(token);
		log.debug("Authorized {} {}",token,ui);
		return ui;
	}
}
