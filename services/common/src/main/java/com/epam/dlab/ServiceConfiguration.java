/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab;

import com.epam.dlab.mongo.MongoServiceFactory;
import com.epam.dlab.rest.client.RESTServiceFactory;
import com.epam.dlab.constants.ServiceConsts;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty(ServiceConsts.MONGO_NAME)
    private MongoServiceFactory mongoFactory = new MongoServiceFactory();
    
    @Valid
    @NotNull
    @JsonProperty(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTServiceFactory provisioningFactory = new RESTServiceFactory();

    @Valid
    @NotNull
    @JsonProperty(ServiceConsts.SECURITY_SERVICE_NAME)
    private RESTServiceFactory securityFactory;

    @Valid
    @NotNull
    @JsonProperty(ServiceConsts.SELF_SERVICE_NAME)
    private RESTServiceFactory selfFactory = new RESTServiceFactory();


    public MongoServiceFactory getMongoFactory() {
        return mongoFactory;
    }
    
    public RESTServiceFactory getProvisioningFactory() {
        return provisioningFactory;
    }

    public RESTServiceFactory getSecurityFactory() {
        return securityFactory;
    }

    public RESTServiceFactory getSelfFactory() {
        return selfFactory;
    }

}
