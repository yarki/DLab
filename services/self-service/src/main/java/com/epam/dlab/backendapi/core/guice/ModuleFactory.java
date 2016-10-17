package com.epam.dlab.backendapi.core.guice;

import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.google.inject.AbstractModule;
import io.dropwizard.setup.Environment;

/**
 * Created by Alexey Suprun
 */
public class ModuleFactory {
    public static AbstractModule getModule(SelfServiceApplicationConfiguration configuration, Environment environment) {
        return configuration.isMocked() ?
                new MockModule(configuration, environment) :
                new ProductionModule(configuration, environment);
    }
}
