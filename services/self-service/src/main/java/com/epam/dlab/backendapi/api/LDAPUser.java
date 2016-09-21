package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey_Suprun on 21-Sep-16.
 */
public class LDAPUser {
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String group;
}
