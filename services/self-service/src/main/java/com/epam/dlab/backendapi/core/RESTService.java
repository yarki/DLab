package com.epam.dlab.backendapi.core;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

/**
 * Created by Alexey Suprun
 */
public class RESTService {
    private Client client;
    private String url;

    public RESTService() {
    }

    RESTService(Client client, String url) {
        this.client = client;
        this.url = url;
    }

    public <T> T get(String path, Class<T> clazz) {
        return getBuilder(path).get(clazz);
    }

    public <T> T post(String path, Object parameter, Class<T> clazz) {
        return getBuilder(path).post(Entity.json(parameter), clazz);
    }

    private Invocation.Builder getBuilder(String path) {
        return client.target(url)
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
