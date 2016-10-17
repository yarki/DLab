package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.response.DirectoriesCreator;
import com.epam.dlab.backendapi.core.response.warmup.DockerWarmuper;
import com.epam.dlab.backendapi.core.response.warmup.MetadataHolder;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.epam.dlab.backendapi.resources.KeyLoaderResource;
import com.epam.dlab.restclient.RESTService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration.SELF_SERVICE;

/**
 * Created by Alexey Suprun
 */
public class ProvisioningServiceApplication extends Application<ProvisioningServiceApplicationConfiguration> {
    public static void main(String[] args) throws Exception {
        new ProvisioningServiceApplication().run(args);
    }

    @Override
    public void run(ProvisioningServiceApplicationConfiguration configuration, Environment environment) throws Exception {
        Injector injector = createInjector(configuration, environment);
        environment.lifecycle().manage(injector.getInstance(DirectoriesCreator.class));
        environment.lifecycle().manage(injector.getInstance(DockerWarmuper.class));
        environment.jersey().register(injector.getInstance(DockerResource.class));
        environment.jersey().register(injector.getInstance(KeyLoaderResource.class));
    }

    private Injector createInjector(ProvisioningServiceApplicationConfiguration configuration, Environment environment) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProvisioningServiceApplicationConfiguration.class).toInstance(configuration);
                bind(MetadataHolder.class).to(DockerWarmuper.class);
                bind(RESTService.class).toInstance(configuration.getSelfFactory().build(environment, SELF_SERVICE));
            }
        });
    }
}
