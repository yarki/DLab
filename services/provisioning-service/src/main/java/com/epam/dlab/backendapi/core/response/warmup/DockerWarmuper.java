package com.epam.dlab.backendapi.core.response.warmup;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.FileHandler;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.dto.ImageMetadataDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class DockerWarmuper implements Managed, DockerCommands, MetadataHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWarmuper.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListenerExecutor folderListenerExecutor;
    @Inject
    private CommandExecuter commandExecuter;
    private Map<String, String> uuids = new ConcurrentHashMap<>();
    private Set<ImageMetadataDTO> metadatas = new ConcurrentHashSet<>();

    @Override
    public void start() throws Exception {
        LOGGER.debug("docker warm up start");
        folderListenerExecutor.start(configuration.getWarmupDirectory(), configuration.getWarmupPollTimeout(), getMetadataHandler());
        List<String> images = commandExecuter.executeSync(GET_IMAGES);
        for (String image : images) {
            LOGGER.debug("image: {}", image);
            String uuid = UUID.randomUUID().toString();
            uuids.put(uuid, image);
            String command = String.format(GET_IMAGE_METADATA, configuration.getKeyDirectory(),
                    configuration.getWarmupDirectory(), uuid, image);
            commandExecuter.executeAsync(command);
        }
    }

    FileHandler getMetadataHandler() {
        return (fileName, content) -> {
            String uuid = DockerCommands.extractUUID(fileName);
            if (uuids.containsKey(uuid)) {
                LOGGER.debug("hadle file {}", fileName);
                ImageMetadataDTO metadata = MAPPER.readValue(content, ImageMetadataDTO.class);
                metadata.setImage(uuids.get(uuid));
                metadatas.add(metadata);
                return true;
            }
            return false;
        };
    }

    @Override
    public void stop() throws Exception {
    }

    public Map<String, String> getUuids() {
        return Collections.unmodifiableMap(uuids);
    }

    public Set<ImageMetadataDTO> getMetadatas() {
        return Collections.unmodifiableSet(metadatas);
    }
}
