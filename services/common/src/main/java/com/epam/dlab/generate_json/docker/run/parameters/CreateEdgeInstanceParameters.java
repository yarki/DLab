/*
package com.epam.dlab.generate_json.docker.run.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;

*/
/**
 * Created by Vladyslav_Valt on 10/20/2016.
 *//*

public class CreateEdgeInstanceParameters implements DockerRunParameters {
    @JsonProperty("conf_service_base_name") private final String confServiceBaseName;
    @JsonProperty("conf_policy_arn") private final String confPolicyArn;
    @JsonProperty("edge_user_name") private final String edgeUserName;
    @JsonProperty("edge_vpc_id") private final String edgeVpcId;
    @JsonProperty("edge_ami_id") private final String edgeAmiId;
    @JsonProperty("edge_instance_size") private final String edgeInstanceSize;
    @JsonProperty("edge_subnet_cidr") private final String edgeSubnetCidr;
    @JsonProperty("edge_notebook_policy_arn") private final String edgeNotebookPolicyArn;
    @JsonProperty("edge_region") private final String edgeRegion;
    @JsonProperty("creds_security_groups_ids") private final String credsSecurityGroupsIds;
    @JsonProperty("creds_key_name") private final String credsKeyName;
    @JsonProperty("creds_subnet_id") private final String credsSubnetId;
    @JsonProperty("action") private final String action;

    private CreateEdgeInstanceParameters(Builder builder) {
        this.confServiceBaseName = builder.confServiceBaseName;
        this.confPolicyArn = builder.confPolicyArn;
        this.edgeUserName = builder.edgeUserName;
        this.edgeVpcId = builder.edgeVpcId;
        this.edgeAmiId = builder.edgeAmiId;
        this.edgeInstanceSize = builder.edgeInstanceSize;
        this.edgeSubnetCidr = builder.edgeSubnetCidr;
        this.edgeNotebookPolicyArn = builder.edgeNotebookPolicyArn;
        this.edgeRegion = builder.edgeRegion;
        this.credsSecurityGroupsIds = builder.credsSecurityGroupsIds;
        this.credsKeyName = builder.credsKeyName;
        this.credsSubnetId = builder.credsSubnetId;
        this.action = builder.action;
    }

    public static Builder newCreateEdgeInstanceParameters() {
        return new Builder();
    }

    public String getConfServiceBaseName() {
        return confServiceBaseName;
    }

    public String getConfPolicyArn() {
        return confPolicyArn;
    }

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public String getEdgeVpcId() {
        return edgeVpcId;
    }

    public String getEdgeAmiId() {
        return edgeAmiId;
    }

    public String getEdgeInstanceSize() {
        return edgeInstanceSize;
    }

    public String getEdgeSubnetCidr() {
        return edgeSubnetCidr;
    }

    public String getEdgeNotebookPolicyArn() {
        return edgeNotebookPolicyArn;
    }

    public String getEdgeRegion() {
        return edgeRegion;
    }

    public String getCredsSecurityGroupsIds() {
        return credsSecurityGroupsIds;
    }

    public String getCredsKeyName() {
        return credsKeyName;
    }

    public String getCredsSubnetId() {
        return credsSubnetId;
    }

    public String getAction() {
        return action;
    }


    public static final class Builder {
        private String confServiceBaseName;
        private String confPolicyArn;
        private String edgeUserName;
        private String edgeVpcId;
        private String edgeAmiId;
        private String edgeInstanceSize;
        private String edgeSubnetCidr;
        private String edgeNotebookPolicyArn;
        private String edgeRegion;
        private String credsSecurityGroupsIds;
        private String credsKeyName;
        private String credsSubnetId;
        private String action = CREATE;

        private Builder() {
        }

        public CreateEdgeInstanceParameters build() {
            return new CreateEdgeInstanceParameters(this);
        }

        public Builder confServiceBaseName(String confServiceBaseName) {
            this.confServiceBaseName = confServiceBaseName;
            return this;
        }

        public Builder confPolicyArn(String confPolicyArn) {
            this.confPolicyArn = confPolicyArn;
            return this;
        }

        public Builder edgeUserName(String edgeUserName) {
            this.edgeUserName = edgeUserName;
            return this;
        }

        public Builder edgeVpcId(String edgeVpcId) {
            this.edgeVpcId = edgeVpcId;
            return this;
        }

        public Builder edgeAmiId(String edgeAmiId) {
            this.edgeAmiId = edgeAmiId;
            return this;
        }

        public Builder edgeInstanceSize(String edgeInstanceSize) {
            this.edgeInstanceSize = edgeInstanceSize;
            return this;
        }

        public Builder edgeSubnetCidr(String edgeSubnetCidr) {
            this.edgeSubnetCidr = edgeSubnetCidr;
            return this;
        }

        public Builder edgeNotebookPolicyArn(String edgeNotebookPolicyArn) {
            this.edgeNotebookPolicyArn = edgeNotebookPolicyArn;
            return this;
        }

        public Builder edgeRegion(String edgeRegion) {
            this.edgeRegion = edgeRegion;
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

        public Builder credsSubnetId(String credsSubnetId) {
            this.credsSubnetId = credsSubnetId;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }
    }
}
*/
