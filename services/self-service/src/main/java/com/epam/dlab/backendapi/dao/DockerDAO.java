package com.epam.dlab.backendapi.dao;

import org.bson.Document;

/**
 * Created by Alexey Suprun
 */
public class DockerDAO extends BaseDAO implements MongoCollections {
    public void writeDockerAttempt() {
        insertOne(DOCKER_ATTEMPTS, () -> new Document("action", "getImages"));
    }
}
