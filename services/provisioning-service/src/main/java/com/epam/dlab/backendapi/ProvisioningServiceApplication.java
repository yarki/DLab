package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.DockerWarmuper;
import com.epam.dlab.backendapi.resources.DockerResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

/**
 * Created by Alexey Suprun
 */
public class ProvisioningServiceApplication extends Application<ProvisioningServiceApplicationConfiguration> {
    public static void main(String[] args) throws Exception {
        new ProvisioningServiceApplication().run(args);
    }

    @Override
    public void run(ProvisioningServiceApplicationConfiguration configuration, Environment environment) throws Exception {
        DockerWarmuper warmuper = new DockerWarmuper(configuration.getResponseDirectory(), configuration.getPollTimeout());
        environment.lifecycle().manage(warmuper);
        environment.jersey().register(new DockerResource(configuration.getResponseDirectory(), warmuper));
    }
}
