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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.Directories;
import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.ICommandExecutor;
import com.epam.dlab.backendapi.core.commands.CommandBuilder;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.backendapi.core.commands.RunDockerCommand;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.backendapi.core.response.handlers.ResourcesStatusCallbackHandler;
import com.epam.dlab.dto.status.EnvResourceDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.google.inject.Inject;

import io.dropwizard.auth.Auth;
import io.dropwizard.util.Duration;

import static com.epam.dlab.backendapi.core.commands.DockerAction.STATUS;

import java.io.IOException;

@Path("/infrastructure")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InfrastructureResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureResource.class);
    
    private static final Duration REQUEST_STATUS_TIMEOUT = Duration.minutes(1);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListenerExecutor folderListenerExecutor;
    @Inject
    private ICommandExecutor commandExecuter;
    @Inject
    private CommandBuilder commandBuilder;
    @Inject
    private RESTService selfService;

    @GET
    @Path("/status")
    public Response status() throws IOException, InterruptedException {
        return Response.status(Response.Status.OK).build();
    }
    
    @Path("/status")
    @POST
    public String status(@Auth UserInfo ui, EnvResourceDTO dto) throws IOException, InterruptedException {
    	LOGGER.debug("Request the status of resources for user {}: {}", ui.getName(), dto);
        String uuid = DockerCommands.generateUUID();
        folderListenerExecutor.start(configuration.getImagesDirectory(),
        		REQUEST_STATUS_TIMEOUT,
                getFileHandlerCallback(STATUS, uuid, dto, ui.getAccessToken()));
        try {
            commandExecuter.executeAsync(
                    ui.getName(),
                    uuid,
                    commandBuilder.buildCommand(
                            new RunDockerCommand()
                                    .withInteractive()
                                    .withName(nameContainer(dto.getEdgeUserName(), STATUS, "resources"))
                                    .withVolumeForRootKeys(configuration.getKeyDirectory())
                                    .withVolumeForResponse(configuration.getImagesDirectory())
                                    .withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
                                    .withRequestId(uuid)
                                    .withConfKeyName(configuration.getAdminKey())
                                    .withActionStatus(configuration.getEdgeImage()),
                            dto
                    )
            );
        } catch (Throwable t) {
            throw new DlabException("Could not create computational resource cluster", t);
        }
        return uuid;
    }

    private FileHandlerCallback getFileHandlerCallback(DockerAction action, String originalUuid, EnvResourceDTO dto, String accessToken) {
        return new ResourcesStatusCallbackHandler(selfService, action, originalUuid, dto, accessToken);
    }

    private String nameContainer(String user, DockerAction action, String name) {
        return nameContainer(user, action.toString(), name);
    }

    public String getResourceType() {
        return Directories.EDGE_LOG_DIRECTORY;
    }
}
