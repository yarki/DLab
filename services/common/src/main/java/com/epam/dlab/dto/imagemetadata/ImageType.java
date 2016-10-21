package com.epam.dlab.dto.imagemetadata;

/**
 * Created by Alexey Suprun
 */
public enum ImageType {
    COMPUTATIONAL("computational"),
    EXPLORATORY("exploratory");

    private String type;

    ImageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
