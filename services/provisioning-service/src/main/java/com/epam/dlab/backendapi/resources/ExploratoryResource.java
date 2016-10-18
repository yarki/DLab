package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.google.inject.Inject;
import org.apache.commons.lang3.NotImplementedException;
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
    private CommandExecuter commandExecuter;

    @Path("/terminate")
    @POST
    public String terminate(String user, String notebook) throws IOException, InterruptedException {
        LOGGER.debug("terminating emr cluster");
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(String.format(TERMINATE_EXPLORATORY_ENVIRONMENT, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid,
                user, // conf_service_base_name
                user, // notebook_user_name
                "creds_region",
                notebook, // notebook_instance_name
                configuration.getAdminKey(), // creds_key_name
                configuration.getEdgeImage()));
        return uuid;
    }

    @Path("/stop")
    @POST
    public String stop(String user, String emr) throws IOException, InterruptedException {
        throw new NotImplementedException("StopExploratoryEnvironment has not been implemented yet");
    }
}
