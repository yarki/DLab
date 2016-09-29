package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.basic.BasicCredentials;
import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class LoginDAO implements MongoCollections {
    @Inject
    private MongoService mongoService;

    public void loginAttempt(BasicCredentials credentials) {
        mongoService.getCollection(LOGIN_ATTEMPT).insertOne(createLoginAttempt(credentials));
    }

    private static Document createLoginAttempt(BasicCredentials credentials) {
        return new Document("login", credentials.getUsername()).append("timestamp", new Date());
    }
}
