package com.epam.dlab.backendapi.dao;

import com.epam.dlab.client.mongo.MongoService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.bson.Document;

import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Created by Alexey Suprun
 */
class BaseDAO implements MongoCollections {
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    public static final String ID = "_id";
    public static final String USER = "user";
    public static final String TIMESTAMP = "timestamp";

    @Inject
    protected MongoService mongoService;

    protected void insertOne(String collection, Supplier<Document> supplier) {
        insertOne(collection, supplier, generateUUID());
    }

    protected void insertOne(String collection, Supplier<Document> supplier, String uuid) {
        mongoService.getCollection(collection).insertOne(supplier.get()
                .append(ID, uuid)
                .append(TIMESTAMP, new Date()));
    }

    protected void insertOne(String collection, Object object) throws JsonProcessingException {
        mongoService.getCollection(collection).insertOne(Document.parse(MAPPER.writeValueAsString(object))
                .append(ID, generateUUID())
                .append(TIMESTAMP, new Date()));
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
