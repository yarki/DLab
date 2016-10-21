package com.epam.dlab.backendapi.dao;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.dto.keyload.KeyLoadStatus;
import com.epam.dlab.dto.keyload.UserAWSCredentialDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.Document;

import java.io.IOException;

import static com.epam.dlab.backendapi.dao.MongoCollections.USER_AWS_CREDENTIALS;
import static com.epam.dlab.backendapi.dao.MongoCollections.USER_KEYS;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by Alexey Suprun
 */
public class KeyDAO extends BaseDAO {
    private static final String STATUS = "status";

    public void uploadKey(final String user, String content) {
        insertOne(USER_KEYS, () -> new Document("content", content).append(STATUS, KeyLoadStatus.NEW.getStatus()), user);
    }

    public void updateKey(String user, String status) {
        mongoService.getCollection(USER_KEYS).updateOne(eq(ID, user), set(STATUS, status));
    }

    public void deleteKey(String user) {
        mongoService.getCollection(USER_KEYS).deleteOne(eq(ID, user));
    }

    public void saveCredential(String user, UserAWSCredentialDTO credential) throws JsonProcessingException {
        insertOne(USER_AWS_CREDENTIALS, credential, user);
    }

    public UserAWSCredentialDTO findCredential(String user) throws IOException {
        return find(USER_AWS_CREDENTIALS, eq(ID, user), UserAWSCredentialDTO.class);
    }

    public KeyLoadStatus findKeyStatus(UserInfo userInfo) {
        Document document = mongoService.getCollection(USER_KEYS).find(eq(ID, userInfo.getName())).first();
        return document != null ? KeyLoadStatus.findByStatus(document.get(STATUS).toString()) : KeyLoadStatus.NONE;
    }
}
