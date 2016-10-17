package com.epam.dlab.backendapi.dao;

import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Alexey Suprun
 */
public class UserNotebookDAO extends BaseDAO {
    public Iterable<Document> find(String user) {
        return mongoService.getCollection(USER_NOTEBOOKS).find(eq(USER, user));
    }

    public void insert(String user, String image) {
        insertOne(USER_NOTEBOOKS, () -> new Document(USER, user).append("image", image));
    }

    public DeleteResult delete(String id) {
        return mongoService.getCollection(USER_NOTEBOOKS).deleteOne(eq(ID, id));
    }
}
