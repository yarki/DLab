package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.MetadataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    private MetadataHolder warmuper;

    public DockerResource(MetadataHolder warmuper) {
        this.warmuper = warmuper;
    }

    @GET
    public Set<ImageMetadata> getDockerImages() throws IOException, InterruptedException {
        LOGGER.debug("docker statuses asked");
        return warmuper.getMetadatas();
    }
}
