package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class EMRCreateDTO extends EMRBaseDTO<EMRCreateDTO> {
    @JsonProperty("emr_instance_count")
    private String instanceCount;
    @JsonProperty("emr_instance_type")
    private String instanceType;
    @JsonProperty("emr_version")
    private String version;
    @JsonProperty("notebook_name")
    private String notebookName;
    @JsonProperty("edge_subnet_cidr")
    private String edgeSubnet;

    public String getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(String instanceCount) {
        this.instanceCount = instanceCount;
    }

    public EMRCreateDTO withInstanceCount(String instanceCount) {
        setInstanceCount(instanceCount);
        return this;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public EMRCreateDTO withInstanceType(String instanceType) {
        setInstanceType(instanceType);
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public EMRCreateDTO withVersion(String version) {
        setVersion(version);
        return this;
    }

    public String getNotebookName() {
        return notebookName;
    }

    public void setNotebookName(String notebookName) {
        this.notebookName = notebookName;
    }

    public EMRCreateDTO withNotebookName(String notebookName) {
        setNotebookName(notebookName);
        return this;
    }

    public String getEdgeSubnet() {
        return edgeSubnet;
    }

    public void setEdgeSubnet(String edgeSubnet) {
        this.edgeSubnet = edgeSubnet;
    }

    public EMRCreateDTO withEdgeSubnet(String edgeSubnet) {
        setEdgeSubnet(edgeSubnet);
        return this;
    }

}
