package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.google.inject.Inject;
import org.bson.Document;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Alexey Suprun
 */
class BaseDAO {
    public static final String TIMESTAMP = "timestamp";

    @Inject
    protected MongoService mongoService;

    protected <T> void insertOne(String collection, Supplier<Document> supplier) {
        mongoService.getCollection(collection).insertOne(supplier.get().append(TIMESTAMP, new Date()));
    }
}
