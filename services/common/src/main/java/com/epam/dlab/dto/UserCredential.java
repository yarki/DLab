package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UserCredential {
    @JsonProperty
    private String username;

    @JsonProperty
    private String password;

    public String getUsername() {
        return username;
    }
}
