package com.epam.dlab.backendapi.core.response.folderlistener;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.response.FileHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.util.Duration;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class FolderListener {
    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;


    public void start(String directory, Duration timeout, FileHandler fileHandler) {
        new FolderListenerThread(directory, timeout, fileHandler, configuration.getFileLengthCheckDelay()).start();
    }
}
