package com.epam.dlab.backendapi.dao;

import static com.epam.dlab.backendapi.dao.MongoCollections.SETTINGS;
import static com.epam.dlab.backendapi.dao.MongoSetting.AWS_REGION;
import static com.mongodb.client.model.Filters.eq;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by Maksym_Pendyshchuk on 10/18/2016.
 */
public class SettingsDAO extends BaseDAO {
    private static String NAME = "name";
    private static String VALUE = "value";

    public String getAwsRegion() {
        return mongoService.getCollection(SETTINGS).find(eq(NAME, AWS_REGION)).first().getOrDefault(VALUE, EMPTY).toString();
    }
}
