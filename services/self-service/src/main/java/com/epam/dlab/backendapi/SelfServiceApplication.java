package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.RESTService;
import com.epam.dlab.backendapi.resources.LoginResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SECURITY_SERVICE;

/**
 * Created by Alexey_Suprun on 20-Sep-16.
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
        RESTService securityService = configuration.getSecurityFactory().build(environment, SECURITY_SERVICE);
        environment.jersey().register(new LoginResource(securityService));
    }
}
