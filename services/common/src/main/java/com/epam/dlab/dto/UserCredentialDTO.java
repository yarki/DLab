package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UserCredentialDTO {
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
    @JsonProperty("access_token")

    private String accessToken;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
