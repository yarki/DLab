package com.epam.dlab.backendapi.core;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static com.epam.dlab.backendapi.core.DockerCommands.GET_IMAGES;
import static com.epam.dlab.backendapi.core.DockerCommands.GET_IMAGE_METADATA;

/**
 * Created by Alexey Suprun
 */
public class DockerWarmuper implements Managed {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWarmuper.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Docker warm up start");
        List<String> images = CommandExecuter.execute(GET_IMAGES);
        for (String image : images) {
            LOGGER.info("Image: {}", image);
            String command = String.format(GET_IMAGE_METADATA, UUID.randomUUID(), image);
            CommandExecuter.execute(command);
        }
    }

    @Override
    public void stop() throws Exception {

    }
}
