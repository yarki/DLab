package com.epam.dlab.backendapi.dao;

import static com.epam.dlab.backendapi.dao.MongoCollections.SETTINGS;
import static com.epam.dlab.backendapi.dao.MongoSetting.AWS_REGION;
import static com.epam.dlab.backendapi.dao.MongoSetting.SERIVICE_BASE_NAME;
import static com.mongodb.client.model.Filters.eq;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by Maksym_Pendyshchuk on 10/18/2016.
 */
public class SettingsDAO extends BaseDAO {
    private static final String VALUE = "value";

    public String getServiceBaseName() {
        return getSetting(SERIVICE_BASE_NAME);
    }

    public String getAwsRegion() {
        return getSetting(AWS_REGION);
    }

    private String getSetting(MongoSetting setting) {
        return mongoService.getCollection(SETTINGS).find(eq(ID, setting.getId())).first().getOrDefault(VALUE, EMPTY).toString();
    }
}
