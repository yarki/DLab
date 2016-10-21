/*
package com.epam.dlab.generate_json.docker.run.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;

;

*/
/**
 * Created by Vladyslav_Valt on 10/20/2016.
 *//*

public class TerminateEMRClusterParameters implements DockerRunParameters {
    @JsonProperty("conf_service_base_name")
    private final String confServiceBaseName;
    @JsonProperty("edge_user_name")
    private final String edgeUserName;
    @JsonProperty("emr_cluster_name")
    private final String emrClusterName;
    @JsonProperty("notebook_instance_name")
    private final String notebookInstanceName;
    @JsonProperty("notebook_ssh_user")
    private final String notebookSshUser;
    @JsonProperty("creds_region")
    private final String credsRegion;
    @JsonProperty("creds_key_dir")
    private final String credsKeyDir;
    @JsonProperty("creds_key_name")
    private final String credsKeyName;
    @JsonProperty("action")
    private final String action;

    private TerminateEMRClusterParameters(Builder builder) {
        this.confServiceBaseName = builder.confServiceBaseName;
        this.edgeUserName = builder.edgeUserName;
        this.emrClusterName = builder.emrClusterName;
        this.notebookInstanceName = builder.notebookInstanceName;
        this.notebookSshUser = builder.notebookSshUser;
        this.credsRegion = builder.credsRegion;
        this.credsKeyDir = builder.credsKeyDir;
        this.credsKeyName = builder.credsKeyName;
        this.action = builder.action;
    }

    public static Builder newTerminateEMRClusterParameters() {
        return new Builder();
    }

    public String getConfServiceBaseName() {
        return confServiceBaseName;
    }

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public String getEmrClusterName() {
        return emrClusterName;
    }

    public String getNotebookInstanceName() {
        return notebookInstanceName;
    }

    public String getNotebookSshUser() {
        return notebookSshUser;
    }

    public String getCredsRegion() {
        return credsRegion;
    }

    public String getCredsKeyDir() {
        return credsKeyDir;
    }

    public String getCredsKeyName() {
        return credsKeyName;
    }

    public String getAction() {
        return action;
    }


    public static final class Builder {
        private String confServiceBaseName;
        private String edgeUserName;
        private String emrClusterName;
        private String notebookInstanceName;
        private String notebookSshUser;
        private String credsRegion;
        private String credsKeyDir;
        private String credsKeyName;
        private String action = TERMINATE;

        private Builder() {
        }

        public TerminateEMRClusterParameters build() {
            return new TerminateEMRClusterParameters(this);
        }

        public Builder confServiceBaseName(String confServiceBaseName) {
            this.confServiceBaseName = confServiceBaseName;
            return this;
        }

        public Builder edgeUserName(String edgeUserName) {
            this.edgeUserName = edgeUserName;
            return this;
        }

        public Builder emrClusterName(String emrClusterName) {
            this.emrClusterName = emrClusterName;
            return this;
        }

        public Builder notebookInstanceName(String notebookInstanceName) {
            this.notebookInstanceName = notebookInstanceName;
            return this;
        }

        public Builder notebookSshUser(String notebookSshUser) {
            this.notebookSshUser = notebookSshUser;
            return this;
        }

        public Builder credsRegion(String credsRegion) {
            this.credsRegion = credsRegion;
            return this;
        }

        public Builder credsKeyDir(String credsKeyDir) {
            this.credsKeyDir = credsKeyDir;
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
