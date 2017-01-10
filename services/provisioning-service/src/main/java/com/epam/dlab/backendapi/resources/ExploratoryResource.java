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
import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.Directories;
import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.ICommandExecutor;
import com.epam.dlab.backendapi.core.commands.*;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.backendapi.core.response.handlers.ExploratoryCallbackHandler;
import com.epam.dlab.dto.exploratory.ExploratoryActionDTO;
import com.epam.dlab.dto.exploratory.ExploratoryBaseDTO;
import com.epam.dlab.dto.exploratory.ExploratoryCreateDTO;
import com.epam.dlab.dto.exploratory.ExploratoryStopDTO;
import com.epam.dlab.rest.client.RESTService;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/exploratory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExploratoryResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExploratoryResource.class);

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


    @Path("/create")
    @POST
    public String create(@Auth UserInfo ui, ExploratoryCreateDTO dto) throws IOException, InterruptedException {
        return action(ui.getName(), ui.getAccessToken(), dto, DockerAction.CREATE);
    }

    @Path("/start")
    @POST
    public String start(@Auth UserInfo ui, ExploratoryActionDTO dto) throws IOException, InterruptedException {
        return action(ui.getName(), ui.getAccessToken(), dto, DockerAction.START);
    }

    @Path("/terminate")
    @POST
    public String terminate(@Auth UserInfo ui, ExploratoryActionDTO dto) throws IOException, InterruptedException {
        return action(ui.getName(), ui.getAccessToken(), dto, DockerAction.TERMINATE);
    }

    @Path("/stop")
    @POST
    public String stop(@Auth UserInfo ui, ExploratoryStopDTO dto) throws IOException, InterruptedException {
        return action(ui.getName(), ui.getAccessToken(), dto, DockerAction.STOP);
    }

    private String action(String username, String accessToken, ExploratoryBaseDTO dto, DockerAction action) throws IOException, InterruptedException {
        LOGGER.debug("{} exploratory environment", action);
        String uuid = DockerCommands.generateUUID();
        folderListenerExecutor.start(configuration.getImagesDirectory(),
                configuration.getResourceStatusPollTimeout(),
                getFileHandlerCallback(action, uuid, dto,accessToken));

        RunDockerCommand runDockerCommand = new RunDockerCommand()
                .withInteractive()
                .withName(nameContainer(dto.getNotebookUserName(), action, dto.getExploratoryName()))
                .withVolumeForRootKeys(configuration.getKeyDirectory())
                .withVolumeForResponse(configuration.getImagesDirectory())
                .withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
                .withResource(getResourceType())
                .withRequestId(uuid)
                .withCredsKeyName(configuration.getAdminKey())
                .withImage(dto.getNotebookImage())
                .withAction(action);

        commandExecuter.executeAsync(username,uuid,commandBuilder.buildCommand(runDockerCommand, dto));
        return uuid;
    }

    private FileHandlerCallback getFileHandlerCallback(DockerAction action, String originalUuid, ExploratoryBaseDTO dto, String accessToken) {
        return new ExploratoryCallbackHandler(selfService, action, originalUuid, dto.getIamUserName(), dto.getExploratoryName(),accessToken);
    }

    private String nameContainer(String user, DockerAction action, String name) {
        return nameContainer(user, action.toString(), "exploratory", name);
    }

    public String getResourceType() {
        return Directories.NOTEBOOK_LOG_DIRECTORY;
    }
}
