package com.epam.dlab.backendapi.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.client.Client;

/**
 * Created by Alexey Suprun
 */
public class RESTServiceFactory {
    @NotEmpty
    @JsonProperty
    private String protocol;

    @NotEmpty
    @JsonProperty
    private String host;

    @Min(1)
    @Max(65535)
    @JsonProperty
    private int port;

    public RESTService build(Environment environment, String name) {
        Client client = new JerseyClientBuilder(environment).build(name);
        return new RESTService(client, getURL());
    }

    private String getURL() {
        return String.format("%s://%s:%d", protocol, host, port);
    }
}
