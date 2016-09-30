package com.epam.dlab.backendapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UserAWSCredential {
    @JsonProperty
    private String user;
    @JsonProperty("tunnel_port")
    private String tunnelPort;
    @JsonProperty("isolated_sg")
    private String isolatedSG;
    @JsonProperty("edge_sg")
    private String edgeSG;
    @JsonProperty
    private String ip;
    @JsonProperty
    private String hostname;
    @JsonProperty("user_own_bicket_name")
    private String userOwnBicketName;
    @JsonProperty("key_name")
    private String keyName;
    @JsonProperty("socks_port")
    private String socksPort;

    public void setUser(String user) {
        this.user = user;
    }
}
