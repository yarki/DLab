/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.response.DirectoriesCreator;
import com.epam.dlab.backendapi.core.response.warmup.DockerWarmuper;
import com.epam.dlab.backendapi.core.response.warmup.MetadataHolder;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.epam.dlab.backendapi.resources.EmrResource;
import com.epam.dlab.backendapi.resources.ExploratoryResource;
import com.epam.dlab.backendapi.resources.KeyLoaderResource;
import com.epam.dlab.client.restclient.RESTService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration.SELF_SERVICE;

public class ProvisioningServiceApplication extends Application<ProvisioningServiceApplicationConfiguration> {
    public static void main(String[] args) throws Exception {
        new ProvisioningServiceApplication().run(args);
    }

    @Override
    public void run(ProvisioningServiceApplicationConfiguration configuration, Environment environment) throws Exception {
        Injector injector = createInjector(configuration, environment);
        environment.lifecycle().manage(injector.getInstance(DirectoriesCreator.class));
        environment.lifecycle().manage(injector.getInstance(DockerWarmuper.class));
        environment.jersey().register(injector.getInstance(DockerResource.class));
        environment.jersey().register(injector.getInstance(KeyLoaderResource.class));
        environment.jersey().register(injector.getInstance(KeyLoaderResource.class));
        environment.jersey().register(injector.getInstance(ExploratoryResource.class));
        environment.jersey().register(injector.getInstance(EmrResource.class));
    }

    private Injector createInjector(ProvisioningServiceApplicationConfiguration configuration, Environment environment) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProvisioningServiceApplicationConfiguration.class).toInstance(configuration);
                bind(MetadataHolder.class).to(DockerWarmuper.class);
                bind(RESTService.class).toInstance(configuration.getSelfFactory().build(environment, SELF_SERVICE));
            }
        });
    }
}
