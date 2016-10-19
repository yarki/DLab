package com.epam.dlab.backendapi.dao;

/**
 * Created by Alexey Suprun
 */
public enum MongoSetting {
    SERIVICE_BASE_NAME("service_base_name"),
    AWS_REGION("aws_region");

    private String id;

    MongoSetting(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
