package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.MetadataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Alexey Suprun
 */
@Path("/docker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    private final String responseDirrectory;
    private final MetadataHolder warmuper;

    public DockerResource(String responseDirrectory, MetadataHolder warmuper) {
        this.responseDirrectory = responseDirrectory;
        this.warmuper = warmuper;
    }

    @GET
    public Set<ImageMetadata> getDockerImages() throws IOException, InterruptedException {
        LOGGER.debug("docker statuses asked");
        return warmuper.getMetadatas();
    }

    @Path("/run")
    @POST
    public String run(String image) throws IOException, InterruptedException {
        LOGGER.debug("run docker image {}", image);
        String uuid = UUID.randomUUID().toString();
        CommandExecuter.execute(String.format(RUN_IMAGE, responseDirrectory, uuid, image));
        return uuid;
    }
}
