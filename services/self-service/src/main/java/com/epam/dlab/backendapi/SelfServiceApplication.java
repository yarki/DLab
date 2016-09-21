package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.RESTClient;
import com.epam.dlab.backendapi.resources.LoginResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.ws.rs.client.Client;

/**
 * Created by Alexey_Suprun on 20-Sep-16.
 */
public class SelfServiceApplication extends Application<SelfServiceApplicationConfiguration> {
    public static final String SECURITY_SERVICE = "security-service";

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
        final Client securityService = new JerseyClientBuilder(environment)
                .using(configuration.getSecurityConfiguration())
                .build(SECURITY_SERVICE);
        environment.jersey().register(new LoginResource(new RESTClient(securityService)));
    }
}
