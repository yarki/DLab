package com.epam.dlab.backendapi.core.response;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;

import java.io.File;

/**
 * Created by Alexey Suprun
 */
public class ResponseDirectoriesCreator implements Managed {
    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;

    @Override
    public void start() throws Exception {
        createDirectory(configuration.getWarmupDirectory());
        createDirectory(configuration.getImagesDirectory());
        createDirectory(configuration.getKeyLoaderDirectory());
    }

    private boolean createDirectory(String directory) {
        return new File(directory).mkdirs();
    }

    @Override
    public void stop() throws Exception {
    }
}
