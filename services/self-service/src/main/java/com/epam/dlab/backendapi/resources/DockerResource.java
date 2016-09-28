package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.epam.dlab.backendapi.client.rest.DockerAPI;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.Set;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

/**
 * Created by Alexey Suprun
 */
@Path("/docker")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements MongoCollections, DockerAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @Inject
    private MongoService mongoService;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @GET
    public Set<ImageMetadata> getDockerImages() {
        LOGGER.debug("docker statuses asked");
        mongoService.getCollection(DOCKER_ATTEMPT).insertOne(new Document("action", "getImages").append("date", new Date()));
        return provisioningService.get(DOCKER, Set.class);
    }

    @Path("/run")
    @POST
    public String run(@FormParam("image") String image) {
        LOGGER.debug("run docker image {}", image);
        return provisioningService.post(DOCKER_RUN, image, String.class);
    }
}
