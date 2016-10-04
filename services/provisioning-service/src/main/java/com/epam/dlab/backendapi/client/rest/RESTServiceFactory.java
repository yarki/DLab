package com.epam.dlab.backendapi.client.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

    @Valid
    @NotNull
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClientConfiguration;

    public RESTService build(Environment environment, String name) {
        Client client = new JerseyClientBuilder(environment).using(jerseyClientConfiguration).build(name);
        return new RESTService(client, getURL());
    }

    private String getURL() {
        return String.format("%s://%s:%d", protocol, host, port);
    }
}
