package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.google.inject.Inject;
import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
public class DockerDAO extends BaseDAO implements MongoCollections {
    @Inject
    private MongoService mongoService;

    public void writeDockerAttempt() {
        mongoService.getCollection(DOCKER_ATTEMPTS).insertOne(createDockerAttempt());
    }

    private Document createDockerAttempt() {
        return new Document("action", "getImages").append(TIMESTAMP, new Date());
    }

}
