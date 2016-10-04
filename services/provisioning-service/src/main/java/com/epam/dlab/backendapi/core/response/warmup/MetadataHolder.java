package com.epam.dlab.backendapi.core.response.warmup;

import com.epam.dlab.backendapi.api.ImageMetadata;

import java.util.Set;

/**
 * Created by Alexey Suprun
 */
public interface MetadataHolder {
    Set<ImageMetadata> getMetadatas();
}
