/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandBuilder;
import com.epam.dlab.backendapi.core.CommandExecutor;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.docker.command.DockerAction;
import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.backendapi.core.response.folderlistener.FileHandlerCallback;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.exploratory.ExploratoryActionDTO;
import com.epam.dlab.dto.exploratory.ExploratoryBaseDTO;
import com.epam.dlab.dto.exploratory.ExploratoryCreateDTO;
import com.epam.dlab.dto.exploratory.ExploratoryStatusDTO;
import com.epam.dlab.dto.keyload.KeyLoadStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static com.epam.dlab.registry.ApiCallbacks.CALLBACK_URI;
import static com.epam.dlab.registry.ApiCallbacks.EXPLORATORY;

@Path("/exploratory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExploratoryResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExploratoryResource.class);

    private static final String STATUS_FIELD = "status";
    private static final String RESPONSE_NODE = "response";
    private static final String RESULT_NODE = "result";

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListenerExecutor folderListenerExecutor;
    @Inject
    private CommandExecutor commandExecuter;
    @Inject
    private CommandBuilder commandBuilder;
    @Inject
    private RESTService selfService;


    @Path("/create")
    @POST
    public String create(ExploratoryCreateDTO dto) throws IOException, InterruptedException {
        return action(dto, DockerAction.CREATE);
    }

    @Path("/start")
    @POST
    public String start(ExploratoryActionDTO dto) throws IOException, InterruptedException {
        return action(dto, DockerAction.START);
    }

    @Path("/terminate")
    @POST
    public String terminate(ExploratoryActionDTO dto) throws IOException, InterruptedException {
        return action(dto, DockerAction.TERMINATE);
    }

    @Path("/stop")
    @POST
    public String stop(ExploratoryActionDTO dto) throws IOException, InterruptedException {
        return action(dto, DockerAction.STOP);
    }

    private String action(ExploratoryBaseDTO dto, DockerAction action) throws IOException, InterruptedException {
        LOGGER.debug("{} exploratory environment", action);
        String uuid = DockerCommands.generateUUID();
        folderListenerExecutor.start(configuration.getImagesDirectory(),
                configuration.getKeyLoaderPollTimeout(),
                getFileHandlerCallback(dto.getNotebookUserName(), uuid, action));
        commandExecuter.executeAsync(
                commandBuilder.buildCommand(
                        new RunDockerCommand()
                                .withInteractive()
                                .withVolumeForRootKeys(configuration.getKeyDirectory())
                                .withVolumeForResponse(configuration.getImagesDirectory())
                                .withRequestId(uuid)
                                .withCredsKeyName(configuration.getAdminKey())
                                .withImage(configuration.getNotebookImage())
                                .withAction(action),
                        dto
                )
        );
        return uuid;
    }

    private FileHandlerCallback getFileHandlerCallback(String user, String originalUuid, DockerAction action) {
        return new FileHandlerCallback() {
            @Override
            public boolean checkUUID(String uuid) {
                return originalUuid.equals(uuid);
            }

            @Override
            public boolean handle(String fileName, byte[] content) throws Exception {
                LOGGER.debug("get file {} actually waited for {}", fileName, originalUuid);
                JsonNode document = MAPPER.readTree(content);
                ExploratoryStatusDTO result = new ExploratoryStatusDTO().withUser(user).withAction(action.toString());
                if (KeyLoadStatus.isSuccess(document.get(STATUS_FIELD).textValue())) {
                    // TODO improve this traversing, maybe having a DTO for response json format + proper path to env_name
                    String envName = document.get(RESPONSE_NODE).get(RESULT_NODE).get("full_edge_conf").get("environment_name").textValue();
                    String instanceName = document.get(RESPONSE_NODE).get(RESULT_NODE).get("full_edge_conf").get("notebook_instance_name").textValue();
                    result = result.withSuccess()
                            .withEnvironmentName(envName)
                            .withNotebookInstanceName(instanceName);
                }
                selfService.post(EXPLORATORY+CALLBACK_URI, result, ExploratoryStatusDTO.class);
                return result.isSuccess();
            }

            @Override
            public void handleError() {
                selfService.post(EXPLORATORY+CALLBACK_URI, new ExploratoryStatusDTO().withUser(user).withAction(action.toString()), ExploratoryStatusDTO.class);
            }
        };
    }

}
