package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
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

    @Path("/terminate")
    @POST
    public String terminateEmr(String user, String emr) throws IOException, InterruptedException {
        LOGGER.debug("terminating emr cluster");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(String.format(TERMINATE_COMPUTATIONAL_RESOURCES, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                user, // conf_service_base_name
                user, // edge_user_name
                emr, // emr_cluster_name
                "creds_region",
                configuration.getAdminKey(), // creds_key_name
                configuration.getEdgeImage()));
        return uuid;
    }
}
