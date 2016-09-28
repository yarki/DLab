package com.epam.dlab.backendapi.core.response.keyloader;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.client.rest.SelfAPI;
import com.epam.dlab.backendapi.core.response.FileHandler;
import com.epam.dlab.backendapi.core.response.FolderListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class KeyLoader implements SelfAPI {
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
            selfService.post(KEY_LOADER, fileName, String.class);
        };
    }
}
