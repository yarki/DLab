/*
package com.epam.dlab.generate_json.docker.run.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;

*/
/**
 * Created by Vladyslav_Valt on 10/20/2016.
 *//*

public class CreateSSNEnvironmentParameters implements DockerRunParameters {
    @JsonProperty("сonf_service_base_name") private final String сonfServiceBaseName;
    @JsonProperty("conf_policy_arn") private final String confPolicyArn;
    @JsonProperty("ssn_ami_id") private final String ssnAmiId;
    @JsonProperty("ssn_instance_size") private final String ssnInstanceSize;
    @JsonProperty("ssn_proxy_port") private final String ssnProxyPort;
    @JsonProperty("ssn_proxy_subnet") private final String ssnProxySubnet;
    @JsonProperty("creds_subnet_id") private final String credsSubnetId;
    @JsonProperty("creds_security_groups_ids") private final String credsSecurityGroupsIds;
    @JsonProperty("creds_access_key") private final String credsAccessKey;
    @JsonProperty("creds_secret_access_key") private final String credsSecretAccessKey;
    @JsonProperty("creds_key_name") private final String credsKeyName;
    @JsonProperty("ops_lifecycle_stage") private final String opsLifecycleStage;
    @JsonProperty("action") private final String action;

    private CreateSSNEnvironmentParameters(Builder builder) {
        this.сonfServiceBaseName = builder.сonfServiceBaseName;
        this.confPolicyArn = builder.confPolicyArn;
        this.ssnAmiId = builder.ssnAmiId;
        this.ssnInstanceSize = builder.ssnInstanceSize;
        this.ssnProxyPort = builder.ssnProxyPort;
        this.ssnProxySubnet = builder.ssnProxySubnet;
        this.credsSubnetId = builder.credsSubnetId;
        this.credsSecurityGroupsIds = builder.credsSecurityGroupsIds;
        this.credsAccessKey = builder.credsAccessKey;
        this.credsSecretAccessKey = builder.credsSecretAccessKey;
        this.credsKeyName = builder.credsKeyName;
        this.opsLifecycleStage = builder.opsLifecycleStage;
        this.action = builder.action;
    }

    public static Builder newCreateSSNEnvironmentParameters() {
        return new Builder();
    }


    public String getСonfServiceBaseName() {
        return сonfServiceBaseName;
    }

    public String getConfPolicyArn() {
        return confPolicyArn;
    }

    public String getSsnAmiId() {
        return ssnAmiId;
    }

    public String getSsnInstanceSize() {
        return ssnInstanceSize;
    }

    public String getSsnProxyPort() {
        return ssnProxyPort;
    }

    public String getSsnProxySubnet() {
        return ssnProxySubnet;
    }

    public String getCredsSubnetId() {
        return credsSubnetId;
    }

    public String getCredsSecurityGroupsIds() {
        return credsSecurityGroupsIds;
    }

    public String getCredsAccessKey() {
        return credsAccessKey;
    }

    public String getCredsSecretAccessKey() {
        return credsSecretAccessKey;
    }

    public String getCredsKeyName() {
        return credsKeyName;
    }

    public String getOpsLifecycleStage() {
        return opsLifecycleStage;
    }

    public String getAction() {
        return action;
    }


    public static final class Builder {
        private String сonfServiceBaseName;
        private String confPolicyArn;
        private String ssnAmiId;
        private String ssnInstanceSize;
        private String ssnProxyPort;
        private String ssnProxySubnet;
        private String credsSubnetId;
        private String credsSecurityGroupsIds;
        private String credsAccessKey;
        private String credsSecretAccessKey;
        private String credsKeyName;
        private String opsLifecycleStage;
        private String action = CREATE;

        private Builder() {
        }

        public CreateSSNEnvironmentParameters build() {
            return new CreateSSNEnvironmentParameters(this);
        }

        public Builder сonfServiceBaseName(String сonfServiceBaseName) {
            this.сonfServiceBaseName = сonfServiceBaseName;
            return this;
        }

        public Builder confPolicyArn(String confPolicyArn) {
            this.confPolicyArn = confPolicyArn;
            return this;
        }

        public Builder ssnAmiId(String ssnAmiId) {
            this.ssnAmiId = ssnAmiId;
            return this;
        }

        public Builder ssnInstanceSize(String ssnInstanceSize) {
            this.ssnInstanceSize = ssnInstanceSize;
            return this;
        }

        public Builder ssnProxyPort(String ssnProxyPort) {
            this.ssnProxyPort = ssnProxyPort;
            return this;
        }

        public Builder ssnProxySubnet(String ssnProxySubnet) {
            this.ssnProxySubnet = ssnProxySubnet;
            return this;
        }

        public Builder credsSubnetId(String credsSubnetId) {
            this.credsSubnetId = credsSubnetId;
            return this;
        }

        public Builder credsSecurityGroupsIds(String credsSecurityGroupsIds) {
            this.credsSecurityGroupsIds = credsSecurityGroupsIds;
            return this;
        }

        public Builder credsAccessKey(String credsAccessKey) {
            this.credsAccessKey = credsAccessKey;
            return this;
        }

        public Builder credsSecretAccessKey(String credsSecretAccessKey) {
            this.credsSecretAccessKey = credsSecretAccessKey;
            return this;
        }

        public Builder credsKeyName(String credsKeyName) {
            this.credsKeyName = credsKeyName;
            return this;
        }

        public Builder opsLifecycleStage(String opsLifecycleStage) {
            this.opsLifecycleStage = opsLifecycleStage;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }
    }
}
*/
