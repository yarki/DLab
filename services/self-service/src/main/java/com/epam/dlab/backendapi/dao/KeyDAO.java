package com.epam.dlab.backendapi.dao;

import com.epam.dlab.dto.keyload.KeyLoadStatus;
import com.epam.dlab.dto.keyload.UserAWSCredentialDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Alexey Suprun
 */
public class KeyDAO extends BaseDAO implements MongoCollections {
    private static final String STATUS = "status";

    public void uploadKey(final String user, String content) {
        insertOne(USER_KEYS, () -> new Document(USER, user).append("content", content).append(STATUS, KeyLoadStatus.NEW.getStatus()));
    }

    public void updateKey(String user, String status) {
        mongoService.getCollection(USER_KEYS).updateOne(eq(USER, user), getUpdater(new Document(STATUS, status)));
    }

    public void saveCredential(UserAWSCredentialDTO credential) throws JsonProcessingException {
        insertOne(USER_AWS_CREDENTIAL, credential);
    }
}
