package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Alexey Suprun
 */
public class LDAPUser {
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private List<String> groups;

    public LDAPUser(String firstName, String lastName, List<String> groups) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.groups = groups;
    }
}
