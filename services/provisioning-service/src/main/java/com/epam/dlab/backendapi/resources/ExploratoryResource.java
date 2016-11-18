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

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandBuilder;
import com.epam.dlab.backendapi.core.CommandExecutor;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.docker.command.DockerAction;
import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.dto.exploratory.ExploratoryActionDTO;
import com.epam.dlab.dto.exploratory.ExploratoryBaseDTO;
import com.epam.dlab.dto.exploratory.ExploratoryCreateDTO;
import com.google.inject.Inject;
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
    private CommandExecutor commandExecutor;

    @Inject
    private CommandBuilder commandBuilder;


    @Path("/create")
    @POST
    public String create(ExploratoryCreateDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("create exploratory environment");
        String uuid = DockerCommands.generateUUID();
        commandExecutor.executeAsync(
                commandBuilder.buildCommand(
                        new RunDockerCommand()
                                .withDetached()
                                .withVolumeForRootKeys(configuration.getKeyDirectory())
                                .withVolumeForResponse(configuration.getImagesDirectory())
                                .withRequestId(uuid)
                                .withConfServiceBaseName(dto.getServiceBaseName())
                                .withNotebookUserName(dto.getNotebookUserName())
                                .withNotebookInstanceType(dto.getNotebookInstanceType())
                                .withCredsRegion(dto.getRegion())
                                .withCredsSecurityGroupsIds(dto.getSecurityGroupIds())
                                .withCredsKeyName(configuration.getAdminKey())
                                .withImage(configuration.getNotebookImage())
                                .withAction(DockerAction.CREATE)
                )
        );
        return uuid;
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
        commandExecutor.executeAsync(
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
}
