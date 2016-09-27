package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.RESTService;
import com.epam.dlab.backendapi.dao.MongoService;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.epam.dlab.backendapi.resources.LoginResource;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;
import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SECURITY_SERVICE;
import static com.epam.dlab.backendapi.core.RESTServiceFactory.DOCKER_SERVICE;

/**
 * Created by Alexey Suprun
 */
public class SelfServiceApplication extends Application<SelfServiceApplicationConfiguration> {
    public static void main(String... args) throws Exception {
        new SelfServiceApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<SelfServiceApplicationConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.addBundle(new AssetsBundle("/webapp/", "/webapp"));
    }

    @Override
    public void run(SelfServiceApplicationConfiguration configuration, Environment environment) throws Exception {
        Injector injector = createInjector(configuration, environment);
        environment.jersey().register(injector.getInstance(LoginResource.class));
        environment.jersey().register(injector.getInstance(DockerResource.class));
    }

    private Injector createInjector(SelfServiceApplicationConfiguration configuration, Environment environment) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                MongoService mongoService = configuration.getMongoFactory().build(environment);
                RESTService securityService = configuration.getSecurityFactory().build(environment, SECURITY_SERVICE);
                RESTService provisioningService = configuration.getProvisioningFactory().build(environment, PROVISIONING_SERVICE);
                bind(MongoService.class).toInstance(mongoService);
                bind(RESTService.class).annotatedWith(Names.named(SECURITY_SERVICE)).toInstance(securityService);
                bind(RESTService.class).annotatedWith(Names.named(DOCKER_SERVICE)).toInstance(provisioningService);
            }
        });
    }
}
