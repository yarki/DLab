package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.Document;

import static com.epam.dlab.backendapi.dao.MongoCollections.SHAPES;
import static com.epam.dlab.backendapi.dao.MongoCollections.USER_INSTANCES;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Alexey Suprun
 */
public class UserListDAO extends BaseDAO {
    public Iterable<Document> find(String user) {
        return mongoService.getCollection(USER_INSTANCES).find(eq(USER, user));
    }

    public Iterable<Document> findShapes() {
        return mongoService.getCollection(SHAPES).find();
    }

    public void insertExploratory(UserInstanceDTO dto) throws JsonProcessingException {
        insertOne(USER_INSTANCES, dto);
    }
}
