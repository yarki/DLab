package com.epam.dlab.backendapi.dao;

/**
 * Created by Alexey Suprun
 */
public enum MongoSetting {
    AWS_REGION("aws_region");

    private String id;

    MongoSetting(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
