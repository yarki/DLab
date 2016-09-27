package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.api.ImageMetadata;

import java.util.Set;

/**
 * Created by Alexey Suprun
 */
public interface MetadataHolder {
    Set<ImageMetadata> getMetadatas();
}
