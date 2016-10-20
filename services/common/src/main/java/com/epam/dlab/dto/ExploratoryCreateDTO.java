package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class ExploratoryCreateDTO extends ExploratoryBaseDTO<ExploratoryCreateDTO> {
    @JsonProperty("notebook_subnet_cidr")
    private String notebookSubnet;
    @JsonProperty("creds_security_groups_ids")
    private String securityGroupIds;
    @JsonProperty("image")
    private String image;

    public String getNotebookSubnet() {
        return notebookSubnet;
    }

    public void setNotebookSubnet(String notebookSubnet) {
        this.notebookSubnet = notebookSubnet;
    }

    public ExploratoryCreateDTO withNotebookSubnet(String notebookSubnet) {
        setNotebookSubnet(notebookSubnet);
        return this;
    }

    public String getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(String securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    public ExploratoryCreateDTO withSecurityGroupIds(String securityGroupIds) {
        setSecurityGroupIds(securityGroupIds);
        return this;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ExploratoryCreateDTO withImage(String image) {
        setImage(image);
        return this;
    }
}
