package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.client.mongo.MongoServiceFactory;
import com.epam.dlab.restclient.RESTServiceFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.epam.dlab.auth.SecurityRestAuthenticator.SECURITY_SERVICE;

/**
 * Created by Alexey Suprun
 */
public class SelfServiceApplicationConfiguration extends Configuration {
    public static final String MONGO = "mongo";

    public static final String PROVISIONING_SERVICE = "provisioningService";

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
    private RESTServiceFactory authenticationFactory;

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

    public RESTServiceFactory getAuthenticationFactory() {
        return authenticationFactory;
    }

    public RESTServiceFactory getProvisioningFactory() {
        return provisioningFactory;
    }
}
