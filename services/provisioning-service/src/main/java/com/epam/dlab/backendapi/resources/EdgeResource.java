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

import static com.epam.dlab.rest.contracts.ApiCallbacks.EDGE;
import static com.epam.dlab.rest.contracts.ApiCallbacks.STATUS_URI;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
import com.epam.dlab.backendapi.core.response.handlers.EdgeCallbackHandler;
import com.epam.dlab.dto.ResourceSysBaseDTO;
import com.epam.dlab.dto.edge.EdgeCreateDTO;
import com.epam.dlab.rest.client.RESTService;
import com.google.inject.Inject;

import io.dropwizard.auth.Auth;

@Path("/edge")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EdgeResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeResource.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListenerExecutor folderListenerExecutor;
    @Inject
    private ICommandExecutor commandExecutor;
    @Inject
    private CommandBuilder commandBuilder;
    @Inject
    private RESTService selfService;


    @Path("/create")
    @POST
    public String create(@Auth UserInfo ui, EdgeCreateDTO dto) throws IOException, InterruptedException {
    	return create(ui.getName(), EDGE + STATUS_URI, dto);
    }

    @Path("/start")
    @POST
    public String start(@Auth UserInfo ui, EdgeCreateDTO dto) throws IOException, InterruptedException {
    	return action(ui.getName(), dto, DockerAction.START);
    }

    @Path("/stop")
    @POST
    public String stop(@Auth UserInfo ui, EdgeCreateDTO dto) throws IOException, InterruptedException {
    	return action(ui.getName(), dto, DockerAction.START);
    }

    public String create(String username, String callbackURI, EdgeCreateDTO dto) throws IOException, InterruptedException {
    	LOGGER.debug("Create EDGE node for user {}: {}", username, dto);
        String uuid = DockerCommands.generateUUID();
        folderListenerExecutor.start(configuration.getKeyLoaderDirectory(),
        		configuration.getKeyLoaderPollTimeout(),
                getFileHandlerCallback(DockerAction.CREATE, uuid, dto.getAwsIamUser(), callbackURI));
        RunDockerCommand runDockerCommand = new RunDockerCommand()
                .withInteractive()
                .withName(nameContainer(dto.getEdgeUserName(), "create", "edge"))
                .withVolumeForRootKeys(configuration.getKeyDirectory())
                .withVolumeForResponse(configuration.getKeyLoaderDirectory())
                .withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
                .withResource(getResourceType())
                .withRequestId(uuid)
                .withConfKeyName(configuration.getAdminKey())
                .withActionCreate(configuration.getEdgeImage());

        commandExecutor.executeAsync(username, uuid, commandBuilder.buildCommand(runDockerCommand,dto));
        return uuid;
    }

    private String action(String username, ResourceSysBaseDTO<?> dto, DockerAction action) throws IOException, InterruptedException {
    	LOGGER.debug("{} EDGE node for user {}: {}", action, username, dto);
        String uuid = DockerCommands.generateUUID();
        folderListenerExecutor.start(configuration.getKeyLoaderDirectory(),
        		configuration.getKeyLoaderPollTimeout(),
                getFileHandlerCallback(action, uuid, dto.getAwsIamUser(), EDGE + STATUS_URI));
        RunDockerCommand runDockerCommand = new RunDockerCommand()
                .withInteractive()
                .withName(nameContainer(dto.getEdgeUserName(), action))
                .withVolumeForRootKeys(configuration.getKeyDirectory())
                .withVolumeForResponse(configuration.getKeyLoaderDirectory())
                .withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
                .withResource(getResourceType())
                .withRequestId(uuid)
                .withConfKeyName(configuration.getAdminKey())
                .withImage(configuration.getEdgeImage())
                .withAction(action);

        commandExecutor.executeAsync(username, uuid, commandBuilder.buildCommand(runDockerCommand,dto));
        return uuid;
    }

    private FileHandlerCallback getFileHandlerCallback(DockerAction action, String uuid, String user, String callbackURI) {
        return new EdgeCallbackHandler(selfService, action, uuid, user, callbackURI);
    }

    private String nameContainer(String user, DockerAction action) {
        return nameContainer(user, action.toString(), "exploratory", getResourceType());
    }
    
    @Override
    public String getResourceType() {
        return Directories.EDGE_LOG_DIRECTORY;
    }
}
