package com.epam.dlab.auth;

import com.google.inject.Injector;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

/**
 * Created by Alexey Suprun
 */
public class SecurityFactory {
    public static final String PREFIX = "Bearer";

    public void configure(Injector injector, Environment environment) {
        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<UserInfo>()
                        .setAuthenticator(injector.getInstance(SecurityRestAuthenticator.class))
                        .setAuthorizer(injector.getInstance(SecurityAuthorizer.class))
                        .setPrefix(PREFIX)
                        .setUnauthorizedHandler(injector.getInstance(SecurityUnauthorizedHandler.class))
                        .buildAuthFilter()));

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserInfo.class));
    }
}
