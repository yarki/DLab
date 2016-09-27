package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.api.ImageMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.dlab.backendapi.core.DockerCommands.GET_IMAGES;
import static com.epam.dlab.backendapi.core.DockerCommands.GET_IMAGE_METADATA;

/**
 * Created by Alexey Suprun
 */
public class DockerWarmuper implements Managed, MetadataHolder {
    private static final String JSON_EXTENTION = ".json";
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWarmuper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String responseDirectory;
    private int pollTimeout;
    private Map<String, String> uuids = new ConcurrentHashMap<>();
    private Set<ImageMetadata> metadatas = new ConcurrentHashSet<>();

    public DockerWarmuper(String responseDirrectory, int pollTimeout) {
        this.responseDirectory = responseDirrectory;
        this.pollTimeout = pollTimeout;
    }

    @Override
    public void start() throws Exception {
        LOGGER.debug("Docker warm up start");
        new FolderListener(responseDirectory, pollTimeout, getMetadataHandler());
        List<String> images = CommandExecuter.execute(GET_IMAGES);
        for (String image : images) {
            LOGGER.debug("Image: {}", image);
            String uuid = UUID.randomUUID().toString();
            uuids.put(uuid, image);
            String command = String.format(GET_IMAGE_METADATA, responseDirectory, uuid, image);
            CommandExecuter.execute(command);
        }

    }

    private FileHandler getMetadataHandler() {
        return fileName -> {
            if (uuids.containsKey(fileName.replace(JSON_EXTENTION, ""))) {
                LOGGER.debug("hadle file {}", fileName);
                ImageMetadata metadata = MAPPER.readValue(readBytes(fileName), ImageMetadata.class);
                metadata.setImage(uuids.get(metadata.getRequestId()));
                metadatas.add(metadata);
            }
        };
    }

    private byte[] readBytes(String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(responseDirectory, fileName));
    }

    @Override
    public void stop() throws Exception {
    }

    public Set<ImageMetadata> getMetadatas() {
        return Collections.unmodifiableSet(metadatas);
    }
}
