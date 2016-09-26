package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alexey Suprun
 */
@Path("/docker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @GET
    public String getDcokerImages() throws IOException, InterruptedException {
        LOGGER.info("Docker status asked");
        List<String> images = CommandExecuter.execute(GET_IMAGES);
        for (String image : images) {
            CommandExecuter.execute(String.format(GET_IMAGE_METADATA, "123", image));
        }
        return "200 OK";
    }
}
