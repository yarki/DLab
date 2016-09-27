package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.core.ProvisioningAPI;
import com.epam.dlab.backendapi.core.RESTService;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.epam.dlab.backendapi.dao.MongoService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.Set;

/**
 * Created by Alexey Suprun
 */
@Path("/docker")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements MongoCollections, ProvisioningAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    private MongoService mongoService;
    private RESTService provisioningService;

    public DockerResource(MongoService mongoService, RESTService provisioningService) {
        this.mongoService = mongoService;
        this.provisioningService = provisioningService;
    }

    @GET
    public Set<ImageMetadata> getDockerImages() {
        LOGGER.debug("docker statuses asked");
        mongoService.getCollection(DOCKER_ATTEMPT).insertOne(new Document("action", "getImages").append("date", new Date()));
        return provisioningService.get(DOCKER, Set.class);
    }
}
