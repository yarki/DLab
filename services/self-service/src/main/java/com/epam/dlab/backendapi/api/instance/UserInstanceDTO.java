package com.epam.dlab.backendapi.api.instance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Alexey Suprun
 */
public class UserInstanceDTO {
    @JsonProperty
    private String user;
    @JsonProperty("environment_name")
    private String environmentName;
    @JsonProperty
    private String status;
    @JsonProperty
    private String shape;
    @JsonProperty
    private String url;
    @JsonProperty("up_time_since")
    private String upTimeSince;
    @JsonProperty("computational_resources")
    private List<UserComputationalResourceDTO> resources;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public UserInstanceDTO withUser(String user) {
        setUser(user);
        return this;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public UserInstanceDTO withEnvironmentName(String environmentName) {
        setEnvironmentName(environmentName);
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserInstanceDTO withStatus(String status) {
        setStatus(status);
        return this;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public UserInstanceDTO withShape(String shape) {
        setShape(shape);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UserInstanceDTO withUrl(String url) {
        setUrl(url);
        return this;
    }

    public String getUpTimeSince() {
        return upTimeSince;
    }

    public void setUpTimeSince(String upTimeSince) {
        this.upTimeSince = upTimeSince;
    }

    public UserInstanceDTO withUpTimeSince(String upTimeSince) {
        setUpTimeSince(upTimeSince);
        return this;
    }

    public List<UserComputationalResourceDTO> getResources() {
        return resources;
    }

    public void setResources(List<UserComputationalResourceDTO> resources) {
        this.resources = resources;
    }

    public UserInstanceDTO withResources(List<UserComputationalResourceDTO> resources) {
        setResources(resources);
        return this;
    }
}
