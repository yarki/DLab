package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.client.rest.KeyLoaderAPI;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.dto.UploadFileDTO;
import com.epam.dlab.dto.UserAWSCredentialDTO;
import com.epam.dlab.restclient.RESTService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    public String post(@FormDataParam("user") String user,
                       @FormDataParam("file") InputStream uploadedInputStream,
                       @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
        LOGGER.debug("upload key for user {}", user);
        String content = "";
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(uploadedInputStream))) {
            content = buffer.lines().collect(Collectors.joining("\n"));
        }
        dao.uploadKey(user, content);
        return provisioningService.post(KEY_LOADER, new UploadFileDTO(user, content), String.class);
    }

    @POST
    public Response loadKeyResponse(UserAWSCredentialDTO credential) {
        LOGGER.debug("credential loaded for user {}", credential.getUser());
        dao.saveCredential(credential);
        return Response.ok().build();
    }
}
