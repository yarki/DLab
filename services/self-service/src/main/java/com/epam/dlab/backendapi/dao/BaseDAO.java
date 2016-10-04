package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.google.inject.Inject;

/**
 * Created by Alexey Suprun
 */
class BaseDAO {
    public static final String TIMESTAMP = "timestamp";

    @Inject
    protected MongoService mongoService;
}
