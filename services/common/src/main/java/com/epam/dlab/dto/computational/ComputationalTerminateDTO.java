/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.dto.computational;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComputationalTerminateDTO extends ComputationalBaseDTO<ComputationalTerminateDTO> {
    @JsonProperty("emr_cluster_name")
    private String clusterName;
    @JsonProperty("notebook_instance_name")
    private String notebookInstanceName;
    @JsonProperty("notebook_ssh_user")
    private String sshUser;
    @JsonProperty("creds_key_dir")
    private String keyDir;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public ComputationalTerminateDTO withClusterName(String clusterName) {
        setClusterName(clusterName);
        return this;
    }

    public String getNotebookInstanceName() {
        return notebookInstanceName;
    }

    public void setNotebookInstanceName(String notebookInstanceName) {
        this.notebookInstanceName = notebookInstanceName;
    }

    public ComputationalTerminateDTO withNotebookInstanceName(String notebookInstanceName) {
        setNotebookInstanceName(notebookInstanceName);
        return this;
    }

    public String getSshUser() {
        return sshUser;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    public ComputationalTerminateDTO withSshUser(String sshUser) {
        setSshUser(sshUser);
        return this;
    }

    public String getKeyDir() {
        return keyDir;
    }

    public void setKeyDir(String keyDir) {
        this.keyDir = keyDir;
    }

    public ComputationalTerminateDTO withKeyDir(String keyDir) {
        setKeyDir(keyDir);
        return this;
    }
}
