package com.epam.dlab.backendapi;

import com.epam.dlab.backendapi.core.response.ResponseDirectories;
import com.epam.dlab.restclient.RESTServiceFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Alexey Suprun
 */
public class ProvisioningServiceApplicationConfiguration extends Configuration implements ResponseDirectories {
    public static final String SELF_SERVICE = "selfService";

    @NotEmpty
    @JsonProperty
    private String keyDirectory;

    @NotEmpty
    @JsonProperty
    private String responseDirectory;

    @JsonProperty
    private Duration warmupPollTimeout = Duration.seconds(3);

    @JsonProperty
    private Duration keyLoaderPollTimeout = Duration.minutes(2);

    @NotEmpty
    @JsonProperty
    private String adminKey;

    @NotEmpty
    @JsonProperty
    private String edgeImage;

    @Valid
    @NotNull
    @JsonProperty(SELF_SERVICE)
    private RESTServiceFactory selfFactory = new RESTServiceFactory();

    public String getKeyDirectory() {
        return keyDirectory;
    }

    public Duration getWarmupPollTimeout() {
        return warmupPollTimeout;
    }

    public Duration getKeyLoaderPollTimeout() {
        return keyLoaderPollTimeout;
    }

    public String getAdminKey() {
        return adminKey;
    }

    public String getEdgeImage() {
        return edgeImage;
    }

    public RESTServiceFactory getSelfFactory() {
        return selfFactory;
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
