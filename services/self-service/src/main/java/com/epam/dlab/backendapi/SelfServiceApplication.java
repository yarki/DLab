package com.epam.dlab.backendapi;

import com.epam.dlab.auth.SecurityFactory;
import com.epam.dlab.backendapi.core.guice.ModuleFactory;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.epam.dlab.backendapi.resources.SecurityResource;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

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
        Injector injector = Guice.createInjector(ModuleFactory.getModule(configuration, environment));
        injector.getInstance(SecurityFactory.class).configure(injector, environment);
        environment.jersey().register(injector.getInstance(SecurityResource.class));
        environment.jersey().register(injector.getInstance(DockerResource.class));
        environment.jersey().register(MultiPartFeature.class);

    }
}
