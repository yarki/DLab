package com.epam.dlab.backendapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Alexey_Suprun on 20-Sep-16.
 */
public class SelfServiceApplicationConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty("security-service")
    private JerseyClientConfiguration securityConfiguration = new JerseyClientConfiguration();

    public JerseyClientConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    public void setSecurityConfiguration(JerseyClientConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }
}
