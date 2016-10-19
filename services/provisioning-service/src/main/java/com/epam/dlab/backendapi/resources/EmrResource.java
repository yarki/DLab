package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.dto.ResourceDTO;
import com.epam.dlab.dto.emr.EMRCreateDTO;
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
    private CommandExecuter commandExecuter;

    @Path("/create")
    @POST
    public String createEmr(EMRCreateDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("create emr cluster");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(String.format(CREATE_EMR_CLUSTER, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
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
                configuration.getEmrImage()));
        return uuid;
    }

    @Path("/terminate")
    @POST
    public String terminateEmr(ResourceDTO emrCluster) throws IOException, InterruptedException {
        LOGGER.debug("terminating emr cluster");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(String.format(TERMINATE_COMPUTATIONAL_RESOURCES, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                emrCluster.getUser(), // conf_service_base_name
                emrCluster.getUser(), // edge_user_name
                emrCluster.getName(), // emr_cluster_name
                emrCluster.getRegion(), // creds_region
                configuration.getAdminKey(), // creds_key_name
                configuration.getEmrImage()));
        return uuid;
    }
}
