package com.epam.dlab.backendapi.dao;

import com.google.inject.Singleton;
import io.dropwizard.auth.basic.BasicCredentials;
import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class LoginDAO extends BaseDAO implements MongoCollections {
    public void writeLoginAttempt(BasicCredentials credentials) {
        mongoService.getCollection(LOGIN_ATTEMPTS).insertOne(createLoginAttempt(credentials));
    }

    private static Document createLoginAttempt(BasicCredentials credentials) {
        return new Document("login", credentials.getUsername()).append(TIMESTAMP, new Date());
    }
}
