package com.epam.dlab.backendapi.dao;

import com.epam.dlab.client.mongo.MongoService;
import com.epam.dlab.dto.keyload.UserAWSCredentialDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

import static com.epam.dlab.backendapi.dao.MongoCollections.USER_AWS_CREDENTIALS;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Alexey Suprun
 */
class BaseDAO {
    protected static final ObjectMapper MAPPER = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
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
        insertOne(collection, object, generateUUID());
    }

    protected void insertOne(String collection, Object object, String uuid) throws JsonProcessingException {
        mongoService.getCollection(collection).insertOne(Document.parse(MAPPER.writeValueAsString(object))
                .append(ID, uuid)
                .append(TIMESTAMP, new Date()));
    }

    protected <T> T find(String collection, Bson eq, Class<T> clazz) throws IOException {
        Document document = mongoService.getCollection(collection).find(eq).first();
        return MAPPER.readValue(document.toString(), clazz);
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
