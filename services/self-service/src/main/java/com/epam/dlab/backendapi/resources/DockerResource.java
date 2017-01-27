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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.DockerDAO;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.DockerAPI;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.dropwizard.auth.Auth;

/** Provides the REST API for the docker.
 */
@Path("/docker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements MongoCollections, DockerAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @Inject
    private DockerDAO dao;
    @Inject
    @Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTService provisioningService;

    /** Sends the request to provisioning service to run docker image for user.
     * @param userInfo user info.
     * @param image image name.
     * @return UUID of request.
     */
    @POST
    @Path("/run")
    public String run(@Auth UserInfo userInfo, @NotBlank String image) throws DlabException {
        LOGGER.debug("Run docker image {} for user {}", image, userInfo.getName());
        try {
        	dao.writeDockerAttempt(userInfo.getName(), DockerDAO.RUN);
        	return provisioningService.post(DOCKER_RUN, userInfo.getAccessToken(), image, String.class);
        } catch (Throwable t) {
        	LOGGER.error("Could not run docker image {} for user {}: {}", image, userInfo.getName(), t.getLocalizedMessage(), t);
            throw new DlabException("Could not run docker image  " + image + " for user " + userInfo.getName(), t);
        }
    }
}
