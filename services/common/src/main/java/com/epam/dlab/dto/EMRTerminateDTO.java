package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
public class EMRTerminateDTO extends EMRBaseDTO<EMRTerminateDTO> {
    @JsonProperty("emr_cluster_name")
    private String clusterName;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public EMRTerminateDTO withClusterName(String clusterName) {
        setClusterName(clusterName);
        return this;
    }

}
