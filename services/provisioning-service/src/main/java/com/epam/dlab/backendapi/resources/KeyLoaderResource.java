package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.core.response.keyloader.KeyLoader;
import com.epam.dlab.dto.UploadFileDTO;
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
 * Created by Alexey Suprun
 */
@Path("/keyloader")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KeyLoaderResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyLoaderResource.class);

    @Inject
    private KeyLoader keyLoader;

    @POST
    public String loadKey(UploadFileDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("load key for user {}", dto.getUser());
        return keyLoader.uploadKey(dto);
    }
}
