package com.epam.dlab.backendapi.dao;

/**
 * Created by Alexey Suprun
 */
public interface MongoCollections {
    String SETTINGS = "settings";
    String LOGIN_ATTEMPTS = "loginAttempts";
    String DOCKER_ATTEMPTS = "dockerAttempts";
    String USER_KEYS = "userKeys";
    String USER_AWS_CREDENTIALS = "userAWSCredentials";
    String USER_NOTEBOOKS = "userNotebooks";
    String SHAPES = "shapes";
}
