package com.epam.dlab.backendapi.core.guice;

import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.google.inject.AbstractModule;
import io.dropwizard.setup.Environment;

/**
 * Created by Alexey Suprun
 */
abstract class BaseModule extends AbstractModule {
    protected SelfServiceApplicationConfiguration configuration;
    protected Environment environment;

    public BaseModule(SelfServiceApplicationConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }
}
