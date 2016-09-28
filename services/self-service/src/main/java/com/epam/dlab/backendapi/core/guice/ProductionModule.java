package com.epam.dlab.backendapi.core.guice;

import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.dropwizard.setup.Environment;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;
import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SECURITY_SERVICE;

/**
 * Created by Alexey Suprun
 */
public class ProductionModule extends AbstractModule {
    private SelfServiceApplicationConfiguration configuration;
    private Environment environment;

    public ProductionModule(SelfServiceApplicationConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(MongoService.class).toInstance(configuration.getMongoFactory().build(environment));
        bind(RESTService.class).annotatedWith(Names.named(SECURITY_SERVICE))
                .toInstance(configuration.getSecurityFactory().build(environment, SECURITY_SERVICE));
        bind(RESTService.class).annotatedWith(Names.named(PROVISIONING_SERVICE))
                .toInstance(configuration.getProvisioningFactory().build(environment, PROVISIONING_SERVICE));
    }
}
