package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.api.UserAWSCredential;
import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
public class KeyDAO extends BaseDAO implements MongoCollections {
    public void uploadKey(String user, String content) {
        mongoService.getCollection(USER_KEYS).insertOne(createUserKey(user, content));
    }

    public void saveCredential(UserAWSCredential credential) {
        mongoService.getCollection(USER_AWS_CREDENTIAL).insertOne(credential.getDocument().append(TIMESTAMP, new Date()));
    }

    private Document createUserKey(String user, String content) {
        return new Document("user", user).append("content", content).append(TIMESTAMP, new Date());
    }
}
