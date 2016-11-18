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
import com.epam.dlab.backendapi.api.form.ExploratoryActionFormDTO;
import com.epam.dlab.backendapi.api.form.ExploratoryCreateFormDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceStatus;
import com.epam.dlab.backendapi.client.rest.ExploratoryAPI;
import com.epam.dlab.backendapi.dao.InfrastructureProvisionDAO;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.StatusBaseDTO;
import com.epam.dlab.dto.exploratory.ExploratoryActionDTO;
import com.epam.dlab.dto.exploratory.ExploratoryCreateDTO;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

@Path("/infrastructure_provision/exploratory_environment")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExploratoryResource implements ExploratoryAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExploratoryResource.class);

    @Inject
    private SettingsDAO settingsDAO;
    @Inject
    private InfrastructureProvisionDAO infrastructureProvisionDAO;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @PUT
    public Response create(@Auth UserInfo userInfo, ExploratoryCreateFormDTO formDTO) {
        LOGGER.debug("creating exploratory environment {} for user {}", formDTO.getName(), userInfo.getName());
        boolean isAdded = infrastructureProvisionDAO.insertExploratory(new UserInstanceDTO()
                .withUser(userInfo.getName())
                .withExploratoryName(formDTO.getName())
                .withStatus(UserInstanceStatus.CREATING.getStatus())
                .withShape(formDTO.getShape()));
        if (isAdded) {
            ExploratoryCreateDTO dto = new ExploratoryCreateDTO()
                    .withServiceBaseName(settingsDAO.getServiceBaseName())
                    .withNotebookUserName(userInfo.getName())
                    .withNotebookInstanceType(formDTO.getShape())
                    .withRegion(settingsDAO.getAwsRegion())
                    .withSecurityGroupIds(settingsDAO.getSecurityGroups());
            LOGGER.debug("created exploratory environment {} for user {}", formDTO.getName(), userInfo.getName());
            return Response
                    .ok(provisioningService.post(EXPLORATORY_CREATE, dto, String.class))
                    .build();
        } else {
            LOGGER.debug("used existing exploratory environment {} for user {}", formDTO.getName(), userInfo.getName());
            return Response.status(Response.Status.FOUND).build();
        }
    }

    @POST
    @Path("/status")
    public Response create(StatusBaseDTO dto) {
        LOGGER.debug("update status for exploratory environment {} for user {}", dto.getExploratoryName(), dto.getUser());
        infrastructureProvisionDAO.updateExploratoryStatus(dto);
        return Response.ok().build();
    }

    @POST
    public String start(@Auth UserInfo userInfo, ExploratoryActionFormDTO formDTO) {
        LOGGER.debug("starting exploratory environment {} for user {}", formDTO.getNotebookInstanceName(), userInfo.getName());
        return action(userInfo, formDTO.getNotebookInstanceName(), EXPLORATORY_START, UserInstanceStatus.RUNNING);
    }

    @DELETE
    @Path("/{name}/stop")
    public String stop(@Auth UserInfo userInfo, @PathParam("name") String name) {
        LOGGER.debug("stopping exploratory environment {} for user {}", name, userInfo.getName());
        return action(userInfo, name, EXPLORATORY_STOP, UserInstanceStatus.STOPPING);
    }

    @DELETE
    @Path("/{name}/terminate")
    public String terminate(@Auth UserInfo userInfo, @PathParam("name") String name) {
        LOGGER.debug("terminating exploratory environment {} for user {}", name, userInfo.getName());
        UserInstanceStatus status = UserInstanceStatus.TERMINATING;
        infrastructureProvisionDAO.updateComputationalStatusesForExploratory(createStatusDTO(userInfo, name, status));
        return action(userInfo, name, EXPLORATORY_TERMINATE, status);
    }

    private String action(UserInfo userInfo, String name, String action, UserInstanceStatus status) {
        infrastructureProvisionDAO.updateExploratoryStatus(createStatusDTO(userInfo, name, status));
        ExploratoryActionDTO dto = new ExploratoryActionDTO()
                .withServiceBaseName(settingsDAO.getServiceBaseName())
                .withNotebookUserName(userInfo.getName())
                .withNotebookInstanceName(name)
                .withRegion(settingsDAO.getAwsRegion());
        return provisioningService.post(action, dto, String.class);
    }

    private StatusBaseDTO createStatusDTO(UserInfo userInfo, String name, UserInstanceStatus status) {
        return new StatusBaseDTO()
                .withUser(userInfo.getName())
                .withExploratoryName(name)
                .withStatus(status.getStatus());
    }
}
