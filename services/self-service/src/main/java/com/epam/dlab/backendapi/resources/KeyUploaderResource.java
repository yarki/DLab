package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.client.rest.KeyLoaderAPI;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.dto.keyload.KeyLoadStatus;
import com.epam.dlab.dto.keyload.UploadFileResultDTO;
import com.epam.dlab.dto.keyload.UploadFileDTO;
import com.epam.dlab.restclient.RESTService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.Auth;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
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


    @GET
    public Response checkKey(@Auth UserInfo userInfo) {
        return Response.status(dao.findKeyStatus(userInfo).getHttpStatus()).build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@Auth UserInfo userInfo,
                       @FormDataParam("file") InputStream uploadedInputStream,
                       @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
        LOGGER.debug("upload key for user {}", userInfo.getName());
        String content = "";
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(uploadedInputStream))) {
            content = buffer.lines().collect(Collectors.joining("\n"));
        }
        dao.uploadKey(userInfo.getName(), content);
        return provisioningService.post(KEY_LOADER, new UploadFileDTO(userInfo.getName(), content), String.class);
    }

    @POST
    public Response loadKeyResponse(UploadFileResultDTO result) throws JsonProcessingException {
        LOGGER.debug("upload key result for user {}", result.getUser(), result.isSuccess());
        dao.updateKey(result.getUser(), KeyLoadStatus.getStatus(result.isSuccess()));
        if (result.isSuccess()) {
            dao.saveCredential(result.getCredential());
        }
        return Response.ok().build();
    }
}
