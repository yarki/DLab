/*
package com.epam.dlab.generate_json.docker.run.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;

*/
/**
 * Created by Vladyslav_Valt on 10/20/2016.
 *//*

public class CreateEMRClusterParameters implements DockerRunParameters {
    @JsonProperty("conf_service_base_name")
    private final String confServiceBaseName;
    @JsonProperty("emr_timeout")
    private final String emrTimeout;
    @JsonProperty("emr_instance_count")
    private final String emrInstanceCount;
    @JsonProperty("emr_instance_type")
    private final String emrInstanceType;
    @JsonProperty("emr_version")
    private final String emrVersion;
    @JsonProperty("ec2_role")
    private final String ec2Role;
    @JsonProperty("service_role")
    private final String serviceRole;
    @JsonProperty("notebook_name")
    private final String notebookName;
    @JsonProperty("edge_user_name")
    private final String edgeUserName;
    @JsonProperty("edge_subnet_cidr")
    private final String edgeSubnetCidr;
    @JsonProperty("creds_region")
    private final String credsRegion;
    @JsonProperty("creds_key_name")
    private final String credsKeyName;
    @JsonProperty("action")
    private final String action;

    private CreateEMRClusterParameters(Builder builder) {
        this.confServiceBaseName = builder.confServiceBaseName;
        this.emrTimeout = builder.emrTimeout;
        this.emrInstanceCount = builder.emrInstanceCount;
        this.emrInstanceType = builder.emrInstanceType;
        this.emrVersion = builder.emrVersion;
        this.ec2Role = builder.ec2Role;
        this.serviceRole = builder.serviceRole;
        this.notebookName = builder.notebookName;
        this.edgeUserName = builder.edgeUserName;
        this.edgeSubnetCidr = builder.edgeSubnetCidr;
        this.credsRegion = builder.credsRegion;
        this.credsKeyName = builder.credsKeyName;
        this.action = builder.action;
    }

    public static Builder newCreateEMRClusterParameters() {
        return new Builder();
    }

    public String getConfServiceBaseName() {
        return confServiceBaseName;
    }

    public String getEmrTimeout() {
        return emrTimeout;
    }

    public String getEmrInstanceCount() {
        return emrInstanceCount;
    }

    public String getEmrInstanceType() {
        return emrInstanceType;
    }

    public String getEmrVersion() {
        return emrVersion;
    }

    public String getEc2Role() {
        return ec2Role;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public String getNotebookName() {
        return notebookName;
    }

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public String getEdgeSubnetCidr() {
        return edgeSubnetCidr;
    }

    public String getCredsRegion() {
        return credsRegion;
    }

    public String getCredsKeyName() {
        return credsKeyName;
    }

    public String getAction() {
        return action;
    }


    public static final class Builder {
        private String confServiceBaseName;
        private String emrTimeout;
        private String emrInstanceCount;
        private String emrInstanceType;
        private String emrVersion;
        private String ec2Role;
        private String serviceRole;
        private String notebookName;
        private String edgeUserName;
        private String edgeSubnetCidr;
        private String credsRegion;
        private String credsKeyName;
        private String action =  CREATE;

        private Builder() {
        }

        public CreateEMRClusterParameters build() {
            return new CreateEMRClusterParameters(this);
        }

        public Builder confServiceBaseName(String confServiceBaseName) {
            this.confServiceBaseName = confServiceBaseName;
            return this;
        }

        public Builder emrTimeout(String emrTimeout) {
            this.emrTimeout = emrTimeout;
            return this;
        }

        public Builder emrInstanceCount(String emrInstanceCount) {
            this.emrInstanceCount = emrInstanceCount;
            return this;
        }

        public Builder emrInstanceType(String emrInstanceType) {
            this.emrInstanceType = emrInstanceType;
            return this;
        }

        public Builder emrVersion(String emrVersion) {
            this.emrVersion = emrVersion;
            return this;
        }

        public Builder ec2Role(String ec2Role) {
            this.ec2Role = ec2Role;
            return this;
        }

        public Builder serviceRole(String serviceRole) {
            this.serviceRole = serviceRole;
            return this;
        }

        public Builder notebookName(String notebookName) {
            this.notebookName = notebookName;
            return this;
        }

        public Builder edgeUserName(String edgeUserName) {
            this.edgeUserName = edgeUserName;
            return this;
        }

        public Builder edgeSubnetCidr(String edgeSubnetCidr) {
            this.edgeSubnetCidr = edgeSubnetCidr;
            return this;
        }

        public Builder credsRegion(String credsRegion) {
            this.credsRegion = credsRegion;
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
