package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.RESTServiceFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Alexey_Suprun on 20-Sep-16.
 */
public class SelfServiceApplicationConfiguration extends Configuration {
    public static final String SECURITY_SERVICE = "security-service";

    @Valid
    @NotNull
    @JsonProperty(SECURITY_SERVICE)
    private RESTServiceFactory securityFactory = new RESTServiceFactory();

    public RESTServiceFactory getSecurityFactory() {
        return securityFactory;
    }
}
