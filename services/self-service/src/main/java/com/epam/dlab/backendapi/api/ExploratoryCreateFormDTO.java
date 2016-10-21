package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class ExploratoryCreateFormDTO {
    @JsonProperty
    private String image;
    @JsonProperty
    private String name;
    @JsonProperty
    private String version;

    public String getImage() {
        return image;
    }
}
