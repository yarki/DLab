package com.epam.dlab.dto.keyload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Alexey Suprun
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAWSCredentialDTO {
    @JsonProperty
    private String hostname;
    @JsonProperty
    private String ip;
    @JsonProperty("key_name")
    private String keyName;
    @JsonProperty("user_own_bicket_name")
    private String userOwnBicketName;
    @JsonProperty("tunnel_port")
    private String tunnelPort;
    @JsonProperty("socks_port")
    private String socksPort;
    @JsonProperty("notebook_sg")
    private String notebookSg;
    @JsonProperty("notebook_profile")
    private String notebookProfile;
    @JsonProperty("notebook_subnet")
    private String notebookSubnet;
    @JsonProperty("edge_sg")
    private String edgeSG;
    @JsonProperty("full_edge_conf")
    private String fullEdgeConf;

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getUserOwnBicketName() {
        return userOwnBicketName;
    }

    public String getTunnelPort() {
        return tunnelPort;
    }

    public String getSocksPort() {
        return socksPort;
    }

    public String getNotebookSg() {
        return notebookSg;
    }

    public String getNotebookProfile() {
        return notebookProfile;
    }

    public String getNotebookSubnet() {
        return notebookSubnet;
    }

    public String getEdgeSG() {
        return edgeSG;
    }

    public String getFullEdgeConf() {
        return fullEdgeConf;
    }
}
