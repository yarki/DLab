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

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.api.form.ComputationalCreateFormDTO;
import com.epam.dlab.backendapi.api.instance.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceStatus;
import com.epam.dlab.backendapi.client.rest.ComputationalAPI;
import com.epam.dlab.backendapi.dao.InfrastructureProvisionDAO;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.computational.ComputationalCreateDTO;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.epam.dlab.dto.computational.ComputationalTerminateDTO;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

@Path("/infrastructure_provision/computational_resources")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ComputationalResource implements ComputationalAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputationalResource.class);

    @Inject
    private SettingsDAO settingsDAO;
    @Inject
    private InfrastructureProvisionDAO infrastructureProvisionDAO;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @PUT
    public Response create(@Auth UserInfo userInfo, ComputationalCreateFormDTO formDTO) {
        LOGGER.debug("creating computational resource {} for user {}", formDTO.getName(), userInfo.getName());
        boolean isAdded = infrastructureProvisionDAO.addComputational(userInfo.getName(), formDTO.getNotebookName(),
                new UserComputationalResourceDTO()
                        .withComputationalName(formDTO.getName())
                        .withStatus(UserInstanceStatus.CREATING.getStatus())
                        .withMasterShape(formDTO.getMasterInstanceType())
                        .withSlaveShape(formDTO.getSlaveInstanceType())
                        .withSlaveNumber(formDTO.getInstanceCount()));
        if (isAdded) {
            ComputationalCreateDTO dto = new ComputationalCreateDTO()
                    .withServiceBaseName(settingsDAO.getServiceBaseName())
                    .withInstanceCount(formDTO.getInstanceCount())
                    .withMasterInstanceType(formDTO.getMasterInstanceType())
                    .withSlaveInstanceType(formDTO.getSlaveInstanceType())
                    .withVersion(formDTO.getVersion())
                    .withNotebookName(formDTO.getNotebookName())
                    .withEdgeUserName(userInfo.getName())
                    .withRegion(settingsDAO.getAwsRegion());
            LOGGER.debug("created computational resource {} for user {}", formDTO.getName(), userInfo.getName());
            return Response
                    .ok(provisioningService.post(EMR_CREATE, dto, String.class))
                    .build();
        } else {
            LOGGER.debug("used existing computational resource {} for user {}", formDTO.getName(), userInfo.getName());
            return Response.status(Response.Status.FOUND).build();
        }
    }

    @POST
    @Path("/status")
    public Response create(ComputationalStatusDTO dto) {
        LOGGER.debug("updating status for computational resource {} for user {}", dto.getComputationalName(), dto.getUser());
        infrastructureProvisionDAO.updateComputationalStatus(dto);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{exploratoryName}/{computationalName}/terminate")
    public String terminate(@Auth UserInfo userInfo, @PathParam("exploratoryName") String exploratoryName, @PathParam("computationalName") String computationalName) {
        LOGGER.debug("terminating computational resource {} for user {}", computationalName, userInfo.getName());
        infrastructureProvisionDAO.updateComputationalStatus(new ComputationalStatusDTO()
                .withUser(userInfo.getName())
                .withExploratoryName(exploratoryName)
                .withComputationalName(computationalName)
                .withStatus(UserInstanceStatus.TERMINATING.getStatus()));
        ComputationalTerminateDTO dto = new ComputationalTerminateDTO()
                .withServiceBaseName(settingsDAO.getServiceBaseName())
                .withEdgeUserName(userInfo.getName())
                .withClusterName(computationalName)
                .withRegion(settingsDAO.getAwsRegion());
        return provisioningService.post(EMR_TERMINATE, dto, String.class);
    }

}
