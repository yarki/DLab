package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.UploadFileDTO;
import com.epam.dlab.backendapi.client.rest.KeyLoaderAPI;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

/**
 * Created by Alexey Suprun
 */
@Path("/keyloader")

@Produces(MediaType.APPLICATION_JSON)
public class KeyUploaderResource implements KeyLoaderAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUploaderResource.class);

    @Inject
    private KeyDAO dao;

    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@FormDataParam("name") String name,
                       @FormDataParam("file") InputStream uploadedInputStream,
                       @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
        LOGGER.debug("upload key");
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(uploadedInputStream))) {
            String content = buffer.lines().collect(Collectors.joining("\n"));
            dao.uploadKey(content);
            provisioningService.post(KEY_LOADER, new UploadFileDTO(name, content), String.class);
        }
        return "200 OK";
    }

    @POST
    public String loadKeyResponse(String fileName) {
        LOGGER.debug("key loaded, result file {}", fileName);
        return "200 OK";
    }
}
