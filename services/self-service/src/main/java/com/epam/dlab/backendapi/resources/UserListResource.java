package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.client.rest.DockerAPI;
import com.epam.dlab.backendapi.dao.UserNotebookDAO;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.imagemetadata.ImageMetadataDTO;
import com.epam.dlab.dto.imagemetadata.ImageType;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.client.result.DeleteResult;
import io.dropwizard.auth.Auth;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

/**
 * Created by Alexey Suprun
 */
@Path("/userlist")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserListResource implements DockerAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUploaderResource.class);

    @Inject
    private UserNotebookDAO dao;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @GET
    public Iterable<Document> getList(@Auth UserInfo userInfo) {
        LOGGER.debug("loading notebooks for user {}", userInfo.getName());
        return dao.find(userInfo.getName());
    }

    @GET
    @Path("/shape")
    public Iterable<Document> getShapes(@Auth UserInfo userInfo) {
        LOGGER.debug("loading shapes for user {}", userInfo.getName());
        return dao.findShapes();
    }

    @GET
    @Path("/computational")
    public Iterable<ImageMetadataDTO> getComputationalTemplates(@Auth UserInfo userInfo) {
        LOGGER.debug("loading computational tempates for user {}", userInfo.getName());
        return getTemplates(ImageType.COMPUTATIONAL);
    }

    @GET
    @Path("/exploratory")
    public Iterable<ImageMetadataDTO> getExploratoryTemplates(@Auth UserInfo userInfo) {
        LOGGER.debug("loading exploratory tempates for user {}", userInfo.getName());
        return getTemplates(ImageType.EXPLORATORY);
    }

    private Iterable<ImageMetadataDTO> getTemplates(ImageType type) {
        return Stream.of(provisioningService.get(DOCKER, ImageMetadataDTO[].class))
                .filter(i -> type.getType().equals(i.getType())).collect(Collectors.toSet());
    }
}
