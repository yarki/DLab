package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class ExploratoryCreateDTO {
    @JsonProperty("conf_service_base_name ")
    private String serviceBaseName;
    @JsonProperty("notebook_user_name")
    private String notebookUserName;
    @JsonProperty("notebook_subnet_cidr")
    private String notebookSubnet;
    @JsonProperty("creds_region")
    private String region;
    @JsonProperty("creds_security_groups_ids")
    private String securityGroupIds;
    @JsonProperty("image")
    private String image;

    public String getServiceBaseName() {
        return serviceBaseName;
    }

    public void setServiceBaseName(String serviceBaseName) {
        this.serviceBaseName = serviceBaseName;
    }

    public String getNotebookUserName() {
        return notebookUserName;
    }

    public void setNotebookUserName(String notebookUserName) {
        this.notebookUserName = notebookUserName;
    }

    public String getNotebookSubnet() {
        return notebookSubnet;
    }

    public void setNotebookSubnet(String notebookSubnet) {
        this.notebookSubnet = notebookSubnet;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(String securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
