package com.epam.dlab.dto.imagemetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class TemplateDTO {
    @JsonProperty
    private String version;

    public TemplateDTO() {
    }

    public TemplateDTO(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
