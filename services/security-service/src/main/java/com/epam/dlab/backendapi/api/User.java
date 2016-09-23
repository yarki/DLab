package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class User {
    @JsonProperty
    private String login;
    @JsonProperty
    private String password;

    @Override
    public String toString() {
        return "{" + login + " " + password + "}";
    }
}
