package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class ExploratoryCreateFormDTO {
    @JsonProperty("image")
    private String image;

    public String getImage() {
        return image;
    }
}
