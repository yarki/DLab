package com.epam.dlab.backendapi.core.response.keyloader;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.client.rest.SelfAPI;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.FileHandler;
import com.epam.dlab.backendapi.core.response.FolderListener;
import com.epam.dlab.dto.keyload.KeyLoadStatus;
import com.epam.dlab.dto.keyload.UploadFileResultDTO;
import com.epam.dlab.dto.keyload.UploadFileDTO;
import com.epam.dlab.dto.keyload.UserAWSCredentialDTO;
import com.epam.dlab.restclient.RESTService;
import com.fasterxml.jackson.databind.JsonNode;
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
    private static final String STATUS_FIELD = "status";
    private static final String RESPONSE_NODE = "response";
    private static final String RESULT_NODE = "result";
    private static final String CONF_NODE = "conf";

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListener folderListener;
    @Inject
    private CommandExecuter commandExecuter;
    @Inject
    private RESTService selfService;

    public String uploadKey(UploadFileDTO dto) throws IOException, InterruptedException {
        saveKeyToFile(dto);
        String uuid = DockerCommands.generateUUID();
        folderListener.start(configuration.getKeyLoaderDirectory(), configuration.getKeyLoaderPollTimeout(),
                getResultHandler(dto.getUser(), uuid));
        commandExecuter.executeAsync(String.format(CREATE_EDGE_METADATA, configuration.getKeyDirectory(),
                configuration.getKeyLoaderDirectory(), uuid, configuration.getAdminKey(),
                dto.getUser(), dto.getUser(), configuration.getEdgeImage()));
        return uuid;
    }

    private void saveKeyToFile(UploadFileDTO dto) throws IOException {
        LOGGER.debug("save key");
        Files.write(Paths.get(configuration.getKeyDirectory(), dto.getUser() + KEY_EXTENTION), dto.getContent().getBytes());
    }

    private FileHandler getResultHandler(final String user, final String uuid) {
        return (fileName, content) -> {
            LOGGER.debug("get file {}", fileName);
            if (uuid.equals(DockerCommands.extractUUID(fileName))) {
                JsonNode document = MAPPER.readTree(content);
                UploadFileResultDTO result = new UploadFileResultDTO(user);
                if (KeyLoadStatus.isSuccess(document.get(STATUS_FIELD).textValue())) {
                    result.setSuccessAndCredential(extractCredential(document));
                }
                selfService.post(KEY_LOADER, result, UploadFileResultDTO.class);
                return true;
            }
            return false;
        };
    }

    private UserAWSCredentialDTO extractCredential(JsonNode document) throws IOException {
        JsonNode node = document.get(RESPONSE_NODE).get(RESULT_NODE).get(CONF_NODE);
        return MAPPER.readValue(node.toString(), UserAWSCredentialDTO.class);
    }
}
