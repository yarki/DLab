package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.client.mongo.MongoServiceFactory;
import com.epam.dlab.backendapi.client.rest.RESTServiceFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Alexey Suprun
 */
public class SelfServiceApplicationConfiguration extends Configuration {
    public static final String MONGO = "mongo";
    public static final String SECURITY_SERVICE = "security-service";
    public static final String PROVISIONING_SERVICE = "provisioning-service";

    @Valid
    @JsonProperty
    private boolean mocked;

    @Valid
    @NotNull
    @JsonProperty(MONGO)
    private MongoServiceFactory mongoFactory = new MongoServiceFactory();

    @Valid
    @NotNull
    @JsonProperty(SECURITY_SERVICE)
    private RESTServiceFactory securityFactory = new RESTServiceFactory();

    @Valid
    @NotNull
    @JsonProperty(PROVISIONING_SERVICE)
    private RESTServiceFactory provisioningFactory = new RESTServiceFactory();

    public boolean isMocked() {
        return mocked;
    }

    public MongoServiceFactory getMongoFactory() {
        return mongoFactory;
    }

    public RESTServiceFactory getSecurityFactory() {
        return securityFactory;
    }

    public RESTServiceFactory getProvisioningFactory() {
        return provisioningFactory;
    }
}
