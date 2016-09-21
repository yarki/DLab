package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey_Suprun on 21-Sep-16.
 */
public class User {
    @JsonProperty
    private String login;
    @JsonProperty
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
