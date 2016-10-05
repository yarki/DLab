package com.epam.dlab.backendapi;

import com.epam.dlab.auth.RestAuthenticator;
import com.epam.dlab.auth.RestAuthenticatorFailureHandler;
import com.epam.dlab.backendapi.core.guice.ModuleFactory;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.epam.dlab.backendapi.resources.LoginResource;
import com.epam.dlab.dto.UserInfo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

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
        createAuth(injector, configuration, environment);
        environment.jersey().register(injector.getInstance(LoginResource.class));
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(injector.getInstance(DockerResource.class));
    }

    private void createAuth(Injector injector, SelfServiceApplicationConfiguration configuration, Environment environment) {
        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<UserInfo>()
                        .setAuthenticator(injector.getInstance(RestAuthenticator.class))
                        .setAuthorizer(new Authorizer<UserInfo>() {
                            @Override
                            public boolean authorize(UserInfo principal, String role) {
//                                TODO: Replace this code when need real roles support. This left here as example
                                return true;
                            }
                        })
                        .setPrefix("Bearer")
                        .buildAuthFilter()));

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserInfo.class));
    }
}
