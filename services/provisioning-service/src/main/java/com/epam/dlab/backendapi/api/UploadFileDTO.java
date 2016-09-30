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

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
