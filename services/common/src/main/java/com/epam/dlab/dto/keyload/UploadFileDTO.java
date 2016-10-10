package com.epam.dlab.dto.keyload;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UploadFileDTO {
    @JsonProperty
    private String user;
    @JsonProperty
    private String content;

    public UploadFileDTO() {
    }

    public UploadFileDTO(String user, String content) {
        this.user = user;
        this.content = content;
    }

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }
}
