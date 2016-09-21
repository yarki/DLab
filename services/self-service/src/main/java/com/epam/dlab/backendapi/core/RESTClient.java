package com.epam.dlab.backendapi.core;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Created by Alexey_Suprun on 21-Sep-16.
 */
public class RESTClient {
    private Client client;

    public RESTClient(Client client) {
        this.client = client;
    }

    public <T> T post(String target, String path, Object parameter, Class<T> clazz) {
        return client.target(target)
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(parameter), clazz);
    }
}
