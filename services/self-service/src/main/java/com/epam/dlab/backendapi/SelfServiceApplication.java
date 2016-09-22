package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.RESTService;
import com.epam.dlab.backendapi.dao.MongoService;
import com.epam.dlab.backendapi.resources.LoginResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SECURITY_SERVICE;

/**
 * Copyright 2016 EPAM Systems, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        MongoService mongoService = configuration.getMongoFactory().build(environment);
        RESTService securityService = configuration.getSecurityFactory().build(environment, SECURITY_SERVICE);
        environment.jersey().register(new LoginResource(mongoService, securityService));
    }
}
