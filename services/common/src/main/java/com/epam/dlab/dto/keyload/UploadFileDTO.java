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
    @JsonProperty("conf_service_base_name ")
    private String serviceBaseName;


    public UploadFileDTO() {
    }

    public UploadFileDTO(String user, String content, String serviceBaseName) {
        this.user = user;
        this.content = content;
        this.serviceBaseName = serviceBaseName;
    }

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public String getServiceBaseName() {
        return serviceBaseName;
    }
}
