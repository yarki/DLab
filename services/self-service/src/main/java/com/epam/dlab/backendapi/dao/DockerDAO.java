package com.epam.dlab.backendapi.dao;

import org.bson.Document;

/**
 * Created by Alexey Suprun
 */
public class DockerDAO extends BaseDAO {
    public static final String DESCRIBE = "describe";
    public static final String RUN = "run";

    public void writeDockerAttempt(String user, String action) {
        insertOne(DOCKER_ATTEMPTS, () -> new Document(USER, user).append("action", action));
    }
}
