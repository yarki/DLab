package com.epam.dlab.backendapi.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexOptions;
import io.dropwizard.lifecycle.Managed;

/**
 * Created by Alexey Suprun
 */
public class IndexCreator extends BaseDAO implements Managed {
    @Override
    public void start() throws Exception {
        mongoService.getCollection(USER_INSTANCES).createIndex(new BasicDBObject(USER, 1).append(ENVIRONMENT_NAME, 2),
                new IndexOptions().unique(true));
    }

    @Override
    public void stop() throws Exception {

    }
}
