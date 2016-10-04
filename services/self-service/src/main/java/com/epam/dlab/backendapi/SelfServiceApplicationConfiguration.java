package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.client.mongo.MongoServiceFactory;
import com.epam.dlab.backendapi.client.rest.RESTServiceFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Alexey Suprun
 */
public class SelfServiceApplicationConfiguration extends Configuration {
    public static final String MONGO = "mongo";
    public static final String SECURITY_SERVICE = "security-service";
    public static final String PROVISIONING_SERVICE = "provisioning-service";
    public static final String AUTHENTICATION_SERVICE_CONFIG = "authenticationServiceConfiguration";
    public static final String JERSEY_CLIENT = "jersey-client";

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

//    @Valid
//    @NotNull
//	@JsonProperty(AUTHENTICATION_SERVICE_CONFIG)
//	private AuthenticationServiceConfig authenticationServiceConfiguration;

    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();

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

//	public AuthenticationServiceConfig getAuthenticationServiceConfiguration() {
//		return authenticationServiceConfiguration;
//	}

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }
}
