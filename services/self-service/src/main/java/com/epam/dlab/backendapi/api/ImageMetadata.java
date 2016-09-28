package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class ImageMetadata {
    @JsonProperty
    private String image;
    @JsonProperty(value = "template_name")
    private String templateName;
    @JsonProperty
    private String description;
    @JsonProperty(value = "request_id")
    private String requestId;

    public ImageMetadata() {
    }

    public ImageMetadata(String image, String templateName, String description, String requestId) {
        this.image = image;
        this.templateName = templateName;
        this.description = description;
        this.requestId = requestId;
    }
}
