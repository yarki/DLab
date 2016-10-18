package com.epam.dlab.client.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Created by Alexey Suprun
 */
public class MongoService {
    private MongoClient client;
    private String database;

    public MongoService(MongoClient client, String database) {
        this.client = client;
        this.database = database;
    }

    public MongoCollection<Document> getCollection(String name) {
        return client.getDatabase(database).getCollection(name, Document.class);
    }
}
