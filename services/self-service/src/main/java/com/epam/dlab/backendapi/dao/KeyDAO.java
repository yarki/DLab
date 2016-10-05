package com.epam.dlab.backendapi.dao;

import com.epam.dlab.dto.UserAWSCredentialDTO;
import org.bson.Document;

/**
 * Created by Alexey Suprun
 */
public class KeyDAO extends BaseDAO implements MongoCollections {
    public void uploadKey(final String user, String content) {
        insertOne(USER_KEYS, () -> new Document("user", user).append("content", content).append("status", "new"));
    }

    public void saveCredential(final UserAWSCredentialDTO credential) {
        insertOne(USER_AWS_CREDENTIAL, credential::getDocument);
    }
}
