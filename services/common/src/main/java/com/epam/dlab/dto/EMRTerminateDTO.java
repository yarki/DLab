package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
public class EMRTerminateDTO extends BaseDTO<EMRTerminateDTO> {

    @JsonProperty("emr_cluster_name")
    private String clusterName;
    @JsonProperty("edge_user_name")
    private String edgeUserName;

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

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public void setEdgeUserName(String edgeUserName) {
        this.edgeUserName = edgeUserName;
    }

    public EMRTerminateDTO withEdgeUserName(String edgeUserName) {
        setEdgeUserName(edgeUserName);
        return this;
    }


}
