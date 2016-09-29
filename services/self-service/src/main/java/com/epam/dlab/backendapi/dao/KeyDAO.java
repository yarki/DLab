package com.epam.dlab.backendapi.dao;

import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
public class KeyDAO extends BaseDAO implements MongoCollections {
    public void uploadKey(String content) {
        mongoService.getCollection(USER_KEYS).insertOne(createUserKey(content));
    }

    private Document createUserKey(String content) {
        return new Document("content", content).append(TIMESTAMP, new Date());
    }
}
