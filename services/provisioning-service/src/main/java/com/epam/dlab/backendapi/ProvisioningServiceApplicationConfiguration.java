package com.epam.dlab.backendapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Created by Alexey Suprun
 */
public class ProvisioningServiceApplicationConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String responseDirrectory;

    @Min(1)
    @Max(100)
    @JsonProperty
    private int pollTimeout;

    public String getResponseDirectory() {
        return responseDirrectory;
    }

    public int getPollTimeout() {
        return pollTimeout;
    }
}
