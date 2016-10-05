package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.Document;

/**
 * Created by Alexey Suprun
 */
public class UserAWSCredentialDTO {
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @JsonIgnore
    public Document getDocument() {
        return new Document("user", user).append("tunnelPort", tunnelPort).append("isolatedSG", isolatedSG)
                .append("edgeSG", edgeSG).append("ip", ip).append("hostname", hostname).append("userOwnBicketName", userOwnBicketName)
                .append("keyName", keyName).append("socksPort", socksPort);
    }
}
