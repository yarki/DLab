package com.epam.dlab.backendapi.dao;

import com.epam.dlab.dto.UserCredentialDTO;
import com.google.inject.Singleton;
import io.dropwizard.auth.basic.BasicCredentials;
import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class SecurityDAO extends BaseDAO implements MongoCollections {
    public void writeLoginAttempt(UserCredentialDTO credentials) {
        insertOne(LOGIN_ATTEMPTS, () -> new Document("login", credentials.getUsername()));
    }
}
