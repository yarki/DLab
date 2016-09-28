package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.client.rest.KeyLoaderAPI;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

/**
 * Created by Alexey Suprun
 */
@Path("/keyloader")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KeyLoaderResource implements KeyLoaderAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @GET
    public void loadKey() {
        LOGGER.debug("load key");
        provisioningService.get(KEY_LOADER, String.class);
    }

    @POST
    public String loadKeyResponse(String fileName) {
        LOGGER.debug("key loaded, result file {}", fileName);
        return "200 OK";
    }
}
