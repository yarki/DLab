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

    public UploadFileDTO(String user, String content) {
        this.user = user;
        this.content = content;
    }
}
