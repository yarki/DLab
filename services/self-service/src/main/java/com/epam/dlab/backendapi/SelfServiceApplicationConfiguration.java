package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.RESTServiceFactory;
import com.epam.dlab.backendapi.dao.MongoServiceFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
public class SelfServiceApplicationConfiguration extends Configuration {
    public static final String MONGO = "mongo";
    public static final String SECURITY_SERVICE = "security-service";

    @Valid
    @NotNull
    @JsonProperty(MONGO)
    private MongoServiceFactory mongoFactory = new MongoServiceFactory();

    @Valid
    @NotNull
    @JsonProperty(SECURITY_SERVICE)
    private RESTServiceFactory securityFactory = new RESTServiceFactory();

    public MongoServiceFactory getMongoFactory() {
        return mongoFactory;
    }

    public RESTServiceFactory getSecurityFactory() {
        return securityFactory;
    }
}
