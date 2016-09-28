package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.response.ResponseDirectories;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Alexey Suprun
 */
public class ProvisioningServiceApplicationConfiguration extends Configuration implements ResponseDirectories {
    @NotEmpty
    @JsonProperty
    private String responseDirectory;

    @JsonProperty
    private Duration warmupPollTimeout = Duration.seconds(3);

    @JsonProperty
    private Duration keyLoaderPollTimeout = Duration.minutes(2);

    public Duration getWarmupPollTimeout() {
        return warmupPollTimeout;
    }

    public Duration getKeyLoaderPollTimeout() {
        return keyLoaderPollTimeout;
    }

    public String getWarmupDirectory() {
        return responseDirectory + WARMUP;
    }

    public String getImagesDirectory() {
        return responseDirectory + IMAGES;
    }

    public String getKeyLoaderDirectory() {
        return responseDirectory + KEY_LOADER;
    }
}
