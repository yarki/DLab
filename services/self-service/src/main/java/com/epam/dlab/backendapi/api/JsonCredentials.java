package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.auth.basic.BasicCredentials;

/**
 * Created by Alexey Suprun
 */
public class JsonCredentials {
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;

    public JsonCredentials(BasicCredentials credentials) {
        this.username = credentials.getUsername();
        this.password = credentials.getPassword();
    }
}
