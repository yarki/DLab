package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandBuilder;
import com.epam.dlab.backendapi.core.CommandExecutor;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.dto.EMRTerminateDTO;
import com.epam.dlab.dto.EMRCreateDTO;
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
@Path("/emr")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmrResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmrResource.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private CommandExecutor commandExecuter;

    @Inject
    private CommandBuilder commandBuilder;

    @Path("/create")
    @POST
    public String create(EMRCreateDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("create emr cluster");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(
                /*String.format(CREATE_EMR_CLUSTER, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                dto.getServiceBaseName(), // conf_service_base_name
                dto.getInstanceCount(), // emr_instance_count
                dto.getInstanceType(), // emr_instance_type
                dto.getVersion(), // emr_version
                configuration.getEmrEC2RoleDefault(), // ec2_role
                configuration.getEmrServiceRoleDefault(), // service_role
                dto.getNotebookName(), // notebook_name
                dto.getEdgeUserName(), // edge_user_name
                dto.getEdgeSubnet(), // edge_subnet_cidr
                dto.getRegion(), // creds_region
                configuration.getAdminKey(), // creds_key_name
                configuration.getEmrImage()));*/
        /*new RunDockerCommand()
                .withVolumeForRootKeys(configuration.getKeyDirectory())
                .withVolumeForResponse(configuration.getImagesDirectory())
                .withRequestId(uuid)
                .withConfServiceBaseName(dto.getServiceBaseName())
                .withEmrInstanceCount(dto.getInstanceCount())
                .withEmrInstanceType( dto.getInstanceType())
                .withEmrVersion( dto.getVersion())
                .withEc2Role(configuration.getEmrEC2RoleDefault())
                .withServiceRole(configuration.getEmrServiceRoleDefault())
                .withNotebookName(dto.getNotebookName())
                .withEdgeUserName(dto.getEdgeUserName())
                .withEdgeSubnetCidr( dto.getEdgeSubnet())
                .withCredsRegion( dto.getRegion())
                .withCredsKeyName(configuration.getAdminKey())
                .withActionCreate(configuration.getEmrImage())
                .toCMD()*/

                commandBuilder.buildCommand(
                        new RunDockerCommand()
                                .withVolumeForRootKeys(configuration.getKeyDirectory())
                                .withVolumeForResponse(configuration.getImagesDirectory())
                                .withRequestId(uuid)
                                .withEc2Role(configuration.getEmrEC2RoleDefault())
                                .withServiceRole(configuration.getEmrServiceRoleDefault())
                                .withCredsKeyName(configuration.getAdminKey())
                                .withActionCreate(configuration.getEmrImage()),
                        dto
                )
        );
        return uuid;
    }

    @Path("/terminate")
    @POST
    public String terminate(EMRTerminateDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("terminate emr cluster");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(

                /*String.format(TERMINATE_EMR_CLUSTER, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                dto.getServiceBaseName(), // conf_service_base_name
                dto.getEdgeUserName(), // edge_user_name
                dto.getClusterName(), // emr_cluster_name
                dto.getRegion(), // creds_region
                configuration.getAdminKey(), // creds_key_name
                configuration.getEmrImage())*/

                /*new RunDockerCommand()
                        .withVolumeForRootKeys(configuration.getKeyDirectory())
                        .withVolumeForResponse(configuration.getImagesDirectory())
                        .withRequestId(uuid)
                        .withConfServiceBaseName(dto.getServiceBaseName())
                        .withEdgeUserName( dto.getEdgeUserName())
                        .withEmrClusterName( dto.getClusterName())
                        .withCredsRegion( dto.getRegion())
                        .withCredsKeyName(configuration.getAdminKey())
                        .withActionTerminate(configuration.getEmrImage())
                .toCMD()*/

                commandBuilder.buildCommand(
                        new RunDockerCommand()
                                .withVolumeForRootKeys(configuration.getKeyDirectory())
                                .withVolumeForResponse(configuration.getImagesDirectory())
                                .withRequestId(uuid)
                                .withCredsKeyName(configuration.getAdminKey())
                                .withActionTerminate(configuration.getEmrImage()),
                        dto
                )
        );
        return uuid;
    }
}
