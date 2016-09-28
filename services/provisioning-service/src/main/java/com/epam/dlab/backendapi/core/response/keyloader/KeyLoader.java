package com.epam.dlab.backendapi.core.response.keyloader;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.client.rest.SelfAPI;
import com.epam.dlab.backendapi.core.response.FileHandler;
import com.epam.dlab.backendapi.core.response.FolderListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class KeyLoader implements SelfAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyLoader.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListener folderListener;
    @Inject
    private RESTService selfService;

    public void loadKey() {
        folderListener.start(configuration.getKeyLoaderDirectory(), configuration.getKeyLoaderPollTimeout(), getResultHandler());
    }

    private FileHandler getResultHandler() {
        return (fileName, bytes) -> {
            LOGGER.debug("get file {}", fileName);
            selfService.post(KEY_LOADER, fileName, String.class);
        };
    }
}
