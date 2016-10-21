package com.epam.dlab.backendapi.api.instance;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UserComputationalResourceDTO {
    @JsonProperty("resource_name")
    private String environmentName;
    @JsonProperty("resource_status")
    private String status;
    @JsonProperty("up_time_since")
    private String upTimeSince;
    @JsonProperty("master_node_shape")
    private String masterShape;
    @JsonProperty("slave_node_shape")
    private String slaveShape;
    @JsonProperty("slave_instance_number")
    private String slaveNumber;

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public UserComputationalResourceDTO withEnvironmentName(String environmentName) {
        setEnvironmentName(environmentName);
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserComputationalResourceDTO withStatus(String status) {
        setStatus(status);
        return this;
    }

    public String getUpTimeSince() {
        return upTimeSince;
    }

    public void setUpTimeSince(String upTimeSince) {
        this.upTimeSince = upTimeSince;
    }

    public UserComputationalResourceDTO withUpTimeSince(String upTimeSince) {
        setUpTimeSince(upTimeSince);
        return this;
    }

    public String getMasterShape() {
        return masterShape;
    }

    public void setMasterShape(String masterShape) {
        this.masterShape = masterShape;
    }

    public UserComputationalResourceDTO withMasterShape(String masterShape) {
        setMasterShape(masterShape);
        return this;
    }

    public String getSlaveShape() {
        return slaveShape;
    }

    public void setSlaveShape(String slaveShape) {
        this.slaveShape = slaveShape;
    }

    public UserComputationalResourceDTO withSlaveShape(String slaveShape) {
        setSlaveShape(slaveShape);
        return this;
    }

    public String getSlaveNumber() {
        return slaveNumber;
    }

    public void setSlaveNumber(String slaveNumber) {
        this.slaveNumber = slaveNumber;
    }

    public UserComputationalResourceDTO withSlaveNumber(String slaveNumber) {
        setSlaveNumber(slaveNumber);
        return this;
    }
}
