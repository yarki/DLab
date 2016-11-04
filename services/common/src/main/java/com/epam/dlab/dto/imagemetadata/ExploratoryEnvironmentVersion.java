package com.epam.dlab.dto.imagemetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Viktor Chukhra <Viktor_Chukhra@epam.com>
 */
public class ExploratoryEnvironmentVersion {
    @JsonProperty(value = "template_name")
    private String templateName;
    @JsonProperty
    private String description;
    @JsonProperty("environment_type")
    private String type;
    @JsonProperty("version")
    private String version;
    @JsonProperty("vendor")
    private String vendor;

    public ExploratoryEnvironmentVersion() {
    }

    public ExploratoryEnvironmentVersion(String templateName, String description, String type, String version,
                                         String vendor) {
        this.templateName = templateName;
        this.description = description;
        this.type = type;
        this.version = version;
        this.vendor = vendor;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
