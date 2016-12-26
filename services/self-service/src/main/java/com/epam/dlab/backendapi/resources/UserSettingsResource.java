/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.backendapi.resources;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SELF_SERVICE;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.UserSettingsDAO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.SelfServiceAPI;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.dropwizard.auth.Auth;


@Path("/user/settings")
@Produces(MediaType.APPLICATION_JSON)
public class UserSettingsResource implements SelfServiceAPI {
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyUploaderResource.class);

    @Inject
    private UserSettingsDAO userSettingsDAO;
    
    @Inject
    @Named(SELF_SERVICE)
    private RESTService selfService;
    
    @GET
    public Response getSettings(@Auth UserInfo userInfo) {
        return Response.ok(
        		selfService.post(USER_SETTINGS,
        				userSettingsDAO.getUISettings(userInfo),
        				String.class))
        			.build();
    }
    
    @POST
    @Path("/save")
    public Response saveSettings(@Auth UserInfo userInfo, @NotBlank String settings) {
        LOGGER.debug("Saves settings for user {}, content is {}", userInfo.getName(), settings);
        try {
        	userSettingsDAO.setUISettings(userInfo, settings);
        } catch (Exception e) {
        	LOGGER.debug("Save settings for user {} fail", userInfo.getName(), e);
        	throw new DlabException("Save settings for user " + userInfo.getName() + " fail", e);
        }
        return Response.ok().build();
    }
}
