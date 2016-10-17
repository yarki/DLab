package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.client.rest.DockerAPI;
import com.epam.dlab.backendapi.dao.DockerDAO;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.ImageMetadataDTO;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

/**
 * Created by Alexey Suprun
 */
@Path("/docker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements MongoCollections, DockerAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @Inject
    private DockerDAO dao;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @GET
    public Set<ImageMetadataDTO> getDockerImages(@Auth UserInfo userInfo) {
        LOGGER.debug("docker statuses asked for user {}", userInfo.getName());
        dao.writeDockerAttempt(userInfo.getName(), DockerDAO.DESCRIBE);
        return provisioningService.get(DOCKER, Set.class);
    }

    @POST
    @Path("/run")
    public String run(@Auth UserInfo userInfo, String image) {
        LOGGER.debug("run docker image {} for user {}", image, userInfo.getName());
        dao.writeDockerAttempt(userInfo.getName(), DockerDAO.RUN);
        return provisioningService.post(DOCKER_RUN, image, String.class);
    }
}
