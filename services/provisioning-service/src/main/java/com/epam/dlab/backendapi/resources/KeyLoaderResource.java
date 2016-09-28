package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.core.response.keyloader.KeyLoader;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

    @GET
    public String loadKey() {
        LOGGER.debug("load key");
        keyLoader.loadKey();
        return "200 OK";
    }
}
