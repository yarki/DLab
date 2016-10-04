package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UploadFileDTO {
    @JsonProperty
    private String user;
    @JsonProperty
    private String content;

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }
}
