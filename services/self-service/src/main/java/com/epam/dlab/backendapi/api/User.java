package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.List;

/**
 * Created by Alexey Suprun
 */
public class User implements Principal {
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private List<String> groups;

    public User() {
    }

    public User(String firstName, String lastName, List<String> groups) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.groups = groups;
    }

    @Override
    public String getName() {
        return firstName;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
