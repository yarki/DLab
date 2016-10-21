package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandBuilder;
import com.epam.dlab.backendapi.core.CommandExecutor;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.dto.ExploratoryCreateDTO;
import com.epam.dlab.dto.ExploratoryTerminateDTO;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by Maksym_Pendyshchuk on 10/17/2016.
 */
@Path("/exploratory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExploratoryResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExploratoryResource.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private CommandExecutor commandExecuter;

    @Inject
    private CommandBuilder commandBuilder;


    @Path("/create")
    @POST
    public String create(ExploratoryCreateDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("create exploratory environment");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(
                /*
                String.format(CREATE_EXPLORATORY_ENVIRONMENT, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                dto.getServiceBaseName(), // conf_service_base_name
                dto.getRegion(), // creds_region
                configuration.getAdminKey(), // creds_key_name
                dto.getNotebookUserName(), // notebook_user_name
                dto.getNotebookSubnet(), // notebook_subnet_cidr
                dto.getSecurityGroupIds(), // creds_security_groups_ids
                dto.getImage()));
                */

                new RunDockerCommand()
                        .withVolumeForRootKeys(configuration.getKeyDirectory())
                        .withVolumeForResponse(configuration.getImagesDirectory())
                        .withRequestId(uuid)
                        .withConfServiceBaseName(dto.getServiceBaseName())
                        .withCredsRegion(dto.getRegion())
                        .withCredsKeyName(configuration.getAdminKey())
                        .withNotebookUserName(dto.getNotebookUserName())
                        .withNotebookSubnetCidr(dto.getNotebookSubnet())
                        .withCredsSecurityGroupsIds(dto.getSecurityGroupIds())
                        .withActionCreate(dto.getImage())
                        .toCMD()

//                commandBuilder.buildCommand(
//                        new RunDockerCommand()
//                                .withVolumeForRootKeys(configuration.getKeyDirectory())
//                                .withVolumeForResponse(configuration.getImagesDirectory())
//                                .withRequestId(uuid)
//                                .withCredsKeyName(configuration.getAdminKey())
//                                .withActionCreate(dto.getImage()),
//                        dto
//                )
        );
        return uuid;
    }

    @Path("/terminate")
    @POST
    public String terminate(ExploratoryTerminateDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("terminate exploratory environment");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(
                /*
                String.format(TERMINATE_EXPLORATORY_ENVIRONMENT, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                dto.getServiceBaseName(), // conf_service_base_name
                dto.getRegion(), // creds_region
                configuration.getAdminKey(), // creds_key_name
                dto.getNotebookUserName(), // notebook_user_name
                dto.getNotebookInstanceName(), // notebook_instance_name
                configuration.getEdgeImage()));
                */

                /*new RunDockerCommand()
                        .withVolumeForRootKeys(configuration.getKeyDirectory())
                        .withVolumeForResponse(configuration.getImagesDirectory())
                        .withRequestId(uuid)
                        .withConfServiceBaseName(dto.getServiceBaseName())
                        .withCredsRegion(dto.getRegion())
                        .withCredsKeyName(configuration.getAdminKey())
                        .withNotebookUserName(dto.getNotebookUserName())
                        .withNotebookInstanceName(dto.getNotebookInstanceName())
                        .withActionTerminate(configuration.getEdgeImage())
                        .toCMD()*/

                commandBuilder.buildCommand(
                        new RunDockerCommand()
                                .withVolumeForRootKeys(configuration.getKeyDirectory())
                                .withVolumeForResponse(configuration.getImagesDirectory())
                                .withRequestId(uuid)
                                .withCredsKeyName(configuration.getAdminKey())
                                .withActionTerminate(configuration.getEdgeImage())
                        ,
                        dto
                )
        );
        return uuid;
    }

    @Path("/stop")
    @POST
    public String stop(ExploratoryTerminateDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("stop exploratory environment");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(
                /*
                String.format(STOP_EXPLORATORY_ENVIRONMENT, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                dto.getServiceBaseName(), // conf_service_base_name
                dto.getRegion(), // creds_region
                configuration.getAdminKey(), // creds_key_name
                dto.getNotebookUserName(), // notebook_user_name
                dto.getNotebookInstanceName(), // notebook_instance_name
                configuration.getEdgeImage())
                */

                /*new RunDockerCommand()
                        .withVolumeForRootKeys(configuration.getKeyDirectory())
                        .withVolumeForResponse(configuration.getImagesDirectory())
                        .withRequestId(uuid)
                        .withConfServiceBaseName(dto.getServiceBaseName())
                        .withCredsRegion(dto.getRegion())
                        .withCredsKeyName(configuration.getAdminKey())
                        .withNotebookUserName(dto.getNotebookUserName())
                        .withNotebookInstanceName(dto.getNotebookInstanceName())
                        .withActionStop(configuration.getEdgeImage())
                        .toCMD()*/

                commandBuilder.buildCommand(
                        new RunDockerCommand()
                                .withVolumeForRootKeys(configuration.getKeyDirectory())
                                .withVolumeForResponse(configuration.getImagesDirectory())
                                .withRequestId(uuid)
                                .withCredsKeyName(configuration.getAdminKey())
                                .withActionStop(configuration.getEdgeImage()),
                        dto
                )
        );
        return uuid;
    }
}
