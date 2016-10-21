/*
package com.epam.dlab.generate_json.docker.run.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;

*/
/**
 * Created by Vladyslav_Valt on 10/20/2016.
 *//*

public class CreateNoteBookServerParameters implements DockerRunParameters {

    @JsonProperty("conf_service_base_name") private final String confServiceBaseName;
    @JsonProperty("notebook_user_name") private final String notebookUserName;
    @JsonProperty("notebook_subnet_cidr") private final String notebookSubnetCidr;
    @JsonProperty("notebook_ami_id") private final String notebookAmiId;
    @JsonProperty("notebook_instance_type") private final String notebookInstanceType;
    @JsonProperty("creds_region") private final String credsRegion;
    @JsonProperty("creds_security_groups_ids") private final String credsSecurityGroupsIds;
    @JsonProperty("creds_key_name") private final String credsKeyName;
    @JsonProperty("action") private final String action;

    private CreateNoteBookServerParameters(Builder builder) {
        this.confServiceBaseName = builder.confServiceBaseName;
        this.notebookUserName = builder.notebookUserName;
        this.notebookSubnetCidr = builder.notebookSubnetCidr;
        this.notebookAmiId = builder.notebookAmiId;
        this.notebookInstanceType = builder.notebookInstanceType;
        this.credsRegion = builder.credsRegion;
        this.credsSecurityGroupsIds = builder.credsSecurityGroupsIds;
        this.credsKeyName = builder.credsKeyName;
        this.action = builder.action;
    }

    public static Builder newCreateNoteBookServerParameters() {
        return new Builder();
    }

    public String getConfServiceBaseName() {
        return confServiceBaseName;
    }

    public String getNotebookUserName() {
        return notebookUserName;
    }

    public String getNotebookSubnetCidr() {
        return notebookSubnetCidr;
    }

    public String getNotebookAmiId() {
        return notebookAmiId;
    }

    public String getNotebookInstanceType() {
        return notebookInstanceType;
    }

    public String getCredsRegion() {
        return credsRegion;
    }

    public String getCredsSecurityGroupsIds() {
        return credsSecurityGroupsIds;
    }

    public String getCredsKeyName() {
        return credsKeyName;
    }

    public String getAction() {
        return action;
    }


    public static final class Builder {
        private String confServiceBaseName;
        private String notebookUserName;
        private String notebookSubnetCidr;
        private String notebookAmiId;
        private String notebookInstanceType;
        private String credsRegion;
        private String credsSecurityGroupsIds;
        private String credsKeyName;
        private String action = CREATE;

        private Builder() {
        }

        public CreateNoteBookServerParameters build() {
            return new CreateNoteBookServerParameters(this);
        }

        public Builder confServiceBaseName(String confServiceBaseName) {
            this.confServiceBaseName = confServiceBaseName;
            return this;
        }

        public Builder notebookUserName(String notebookUserName) {
            this.notebookUserName = notebookUserName;
            return this;
        }

        public Builder notebookSubnetCidr(String notebookSubnetCidr) {
            this.notebookSubnetCidr = notebookSubnetCidr;
            return this;
        }

        public Builder notebookAmiId(String notebookAmiId) {
            this.notebookAmiId = notebookAmiId;
            return this;
        }

        public Builder notebookInstanceType(String notebookInstanceType) {
            this.notebookInstanceType = notebookInstanceType;
            return this;
        }

        public Builder credsRegion(String credsRegion) {
            this.credsRegion = credsRegion;
            return this;
        }

        public Builder credsSecurityGroupsIds(String credsSecurityGroupsIds) {
            this.credsSecurityGroupsIds = credsSecurityGroupsIds;
            return this;
        }

        public Builder credsKeyName(String credsKeyName) {
            this.credsKeyName = credsKeyName;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }
    }
}
*/
