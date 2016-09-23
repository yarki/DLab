package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.Document;

import java.util.Date;

/**
 * Created by Alexey Suprun
 */
public class User {
    @JsonProperty
    private String login;
    @JsonProperty
    private String password;
    private Date date = new Date();

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @JsonIgnore
    public Document getDocument() {
        return new Document("login", login)
                .append("timestamp", date);
    }
}
