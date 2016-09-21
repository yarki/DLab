package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.resources.LoginResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

/**
 * Created by Alexey_Suprun on 21-Sep-16.
 */
public class SecurityServiceApplication extends Application<SecurityServiceApplicationConfiguration> {
    public static void main(String... args) throws Exception {
        new SecurityServiceApplication().run(args);
    }

    @Override
    public void run(SecurityServiceApplicationConfiguration securityServiceApplicationConfiguration, Environment environment) throws Exception {
        environment.jersey().register(new LoginResource());
    }
}
