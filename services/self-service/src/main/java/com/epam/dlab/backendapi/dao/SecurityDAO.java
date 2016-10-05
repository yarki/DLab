package com.epam.dlab.backendapi.dao;

import com.epam.dlab.dto.UserCredentialDTO;
import com.google.inject.Singleton;
import org.bson.Document;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class SecurityDAO extends BaseDAO implements MongoCollections {
    public void writeLoginAttempt(UserCredentialDTO credentials) {
        insertOne(LOGIN_ATTEMPTS, () -> new Document("login", credentials.getUsername()));
    }
}
