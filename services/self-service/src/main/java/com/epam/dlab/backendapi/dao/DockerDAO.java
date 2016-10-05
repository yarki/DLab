package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.google.inject.Inject;
import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
public class DockerDAO extends BaseDAO implements MongoCollections {
    public void writeDockerAttempt() {
        insertOne(DOCKER_ATTEMPTS, () -> new Document("action", "getImages"));
    }
}
