package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UploadFileDTO {
    @JsonProperty
    private String name;
    @JsonProperty
    private String content;

    public UploadFileDTO(String name, String content) {
        this.name = name;
        this.content = content;
    }
}
