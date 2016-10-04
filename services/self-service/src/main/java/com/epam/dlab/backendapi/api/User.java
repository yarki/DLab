package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by Alexey Suprun
 */
public class User implements Principal {
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private Collection<String> groups;

    public User() {
    }

    public User(String firstName, String lastName, Collection<String> groups) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.groups = groups;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return firstName;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
