package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class EMRCreateFormDTO {
    @JsonProperty("emr_instance_count")
    private String instanceCount;
    @JsonProperty("emr_instance_type")
    private String instanceType;
    @JsonProperty("emr_version")
    private String version;
    @JsonProperty("notebook_name")
    private String notebookName;

    public String getInstanceCount() {
        return instanceCount;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public String getVersion() {
        return version;
    }

    public String getNotebookName() {
        return notebookName;
    }
}
