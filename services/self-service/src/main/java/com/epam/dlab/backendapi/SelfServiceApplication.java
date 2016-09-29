package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.api.User;
import com.epam.dlab.backendapi.auth.SelfServiceAuthenticator;
import com.epam.dlab.backendapi.auth.SelfServiceAuthorizer;
import com.epam.dlab.backendapi.core.guice.ModuleFactory;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.epam.dlab.backendapi.resources.KeyLoaderResource;
import com.epam.dlab.backendapi.resources.LoginResource;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
        environment.jersey().register(createAuth(injector));
        environment.jersey().register(injector.getInstance(DockerResource.class));
        environment.jersey().register(injector.getInstance(KeyLoaderResource.class));
    }

    private AuthDynamicFeature createAuth(Injector injector) {
        return new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(injector.getInstance(SelfServiceAuthenticator.class))
                        .setAuthorizer(injector.getInstance(SelfServiceAuthorizer.class))
                        .setRealm("SUPER SECRET STUFF")
                        .buildAuthFilter());
    }
}
