package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.warmup.MetadataHolder;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Alexey Suprun
 */
@Path("/docker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private MetadataHolder metadataHolder;
    @Inject
    private CommandExecuter commandExecuter;

    @GET
    public Set<ImageMetadata> getDockerImages() throws IOException, InterruptedException {
        LOGGER.debug("docker statuses asked");
        return metadataHolder.getMetadatas();
    }

    @Path("/run")
    @POST
    public String run(String image) throws IOException, InterruptedException {
        LOGGER.debug("run docker image {}", image);
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(String.format(RUN_IMAGE, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid, image));
        return uuid;
    }
}
