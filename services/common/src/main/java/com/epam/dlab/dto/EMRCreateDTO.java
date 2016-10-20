package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class EMRCreateDTO {
    @JsonProperty("conf_service_base_name ")
    private String serviceBaseName;
    @JsonProperty("emr_instance_count")
    private String instanceCount;
    @JsonProperty("emr_instance_type")
    private String instanceType;
    @JsonProperty("emr_version")
    private String version;
    @JsonProperty("notebook_name")
    private String notebookName;
    @JsonProperty("edge_user_name")
    private String edgeUserName;
    @JsonProperty("edge_subnet_cidr")
    private String edgeSubnet;
    @JsonProperty("creds_region")
    private String region;

    public String getServiceBaseName() {
        return serviceBaseName;
    }

    public void setServiceBaseName(String serviceBaseName) {
        this.serviceBaseName = serviceBaseName;
    }

    public String getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(String instanceCount) {
        this.instanceCount = instanceCount;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNotebookName() {
        return notebookName;
    }

    public void setNotebookName(String notebookName) {
        this.notebookName = notebookName;
    }

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public void setEdgeUserName(String edgeUserName) {
        this.edgeUserName = edgeUserName;
    }

    public String getEdgeSubnet() {
        return edgeSubnet;
    }

    public void setEdgeSubnet(String edgeSubnet) {
        this.edgeSubnet = edgeSubnet;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
