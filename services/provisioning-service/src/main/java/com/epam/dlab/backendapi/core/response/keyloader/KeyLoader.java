package com.epam.dlab.backendapi.core.response.keyloader;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.api.UploadFileDTO;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.client.rest.SelfAPI;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.FileHandler;
import com.epam.dlab.backendapi.core.response.FolderListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class KeyLoader implements DockerCommands, SelfAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyLoader.class);
    private static final String KEY_EXTENTION = ".pem";

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListener folderListener;
    @Inject
    private CommandExecuter commandExecuter;
    @Inject
    private RESTService selfService;

    public void uploadKey(UploadFileDTO dto) throws IOException, InterruptedException {
        saveKeyToFile(dto);
        String uuid = DockerCommands.generateUUID();
        folderListener.start(configuration.getKeyLoaderDirectory(), configuration.getKeyLoaderPollTimeout(), getResultHandler(uuid));
        commandExecuter.execute(String.format(CREATE_EDGE_METADATA, configuration.getKeyDirectory(),
                configuration.getKeyLoaderDirectory(), uuid, configuration.getAdminKey(),
                dto.getName(), dto.getName(), configuration.getEdgeImage()), true);
    }

    private void saveKeyToFile(UploadFileDTO dto) throws IOException {
        LOGGER.debug("save key");
        Files.write(Paths.get(configuration.getKeyDirectory(), dto.getName() + KEY_EXTENTION), dto.getContent().getBytes());
    }

    private FileHandler getResultHandler(final String uuid) {
        return (fileName, bytes) -> {
            LOGGER.debug("get file {}", fileName);
            if (uuid.equals(DockerCommands.extractUUID(fileName))) {
                selfService.post(KEY_LOADER, fileName, String.class);
            }
        };
    }
}
