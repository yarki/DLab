package com.epam.dlab.backendapi.core.response.warmup;

import com.epam.dlab.dto.ImageMetadataDTO;

import java.util.Set;

/**
 * Created by Alexey Suprun
 */
public interface MetadataHolder {
    Set<ImageMetadataDTO> getMetadatas();
}
