package com.epam.dlab.backendapi.api.form;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
public class EMRTerminateFormDTO {
    @JsonProperty("emr_cluster_name")
    private String clusterName;

    public String getClusterName() {
        return clusterName;
    }
}
