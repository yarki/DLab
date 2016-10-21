//package com.epam.dlab.generate_json.docker.run.parameters;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//
///**
// * Created by Vladyslav_Valt on 10/20/2016.
// */
//public class StopExploratoryEnvironmentParameters implements DockerRunParameters {
//    @JsonProperty("conf_service_base_name") private final String confServiceBaseName;
//    @JsonProperty("notebook_user_name") private final String notebookUserName;
//    @JsonProperty("creds_region") private final String credsRegion;
//    @JsonProperty("notebook_instance_name") private final String notebookInstanceName;
//    @JsonProperty("creds_key_name") private final String credsKeyName;
//    @JsonProperty("action") private final String action;
//
//    private StopExploratoryEnvironmentParameters(Builder builder) {
//        this.confServiceBaseName = builder.confServiceBaseName;
//        this.notebookUserName = builder.notebookUserName;
//        this.credsRegion = builder.credsRegion;
//        this.notebookInstanceName = builder.notebookInstanceName;
//        this.credsKeyName = builder.credsKeyName;
//        this.action = builder.action;
//    }
//
//    public static Builder newStopExploratoryEnvironmentParameters() {
//        return new Builder();
//    }
//
//    public String getConfServiceBaseName() {
//        return confServiceBaseName;
//    }
//
//    public String getNotebookUserName() {
//        return notebookUserName;
//    }
//
//    public String getCredsRegion() {
//        return credsRegion;
//    }
//
//    public String getNotebookInstanceName() {
//        return notebookInstanceName;
//    }
//
//    public String getCredsKeyName() {
//        return credsKeyName;
//    }
//
//    public String getAction() {
//        return action;
//    }
//
//
//    public static final class Builder {
//        private String confServiceBaseName;
//        private String notebookUserName;
//        private String credsRegion;
//        private String notebookInstanceName;
//        private String credsKeyName;
//        private String action = STOP;
//
//        private Builder() {
//        }
//
//        public StopExploratoryEnvironmentParameters build() {
//            return new StopExploratoryEnvironmentParameters(this);
//        }
//
//        public Builder confServiceBaseName(String confServiceBaseName) {
//            this.confServiceBaseName = confServiceBaseName;
//            return this;
//        }
//
//        public Builder notebookUserName(String notebookUserName) {
//            this.notebookUserName = notebookUserName;
//            return this;
//        }
//
//        public Builder credsRegion(String credsRegion) {
//            this.credsRegion = credsRegion;
//            return this;
//        }
//
//        public Builder notebookInstanceName(String notebookInstanceName) {
//            this.notebookInstanceName = notebookInstanceName;
//            return this;
//        }
//
//        public Builder credsKeyName(String credsKeyName) {
//            this.credsKeyName = credsKeyName;
//            return this;
//        }
//
//        public Builder action(String action) {
//            this.action = action;
//            return this;
//        }
//    }
//}
