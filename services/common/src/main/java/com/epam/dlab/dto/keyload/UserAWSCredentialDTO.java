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

package com.epam.dlab.dto.keyload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAWSCredentialDTO {
    @JsonProperty
    private String hostname;
    @JsonProperty
    private String ip;
    @JsonProperty("key_name")
    private String keyName;
    @JsonProperty("user_own_bicket_name")
    private String userOwnBicketName;
    @JsonProperty("tunnel_port")
    private String tunnelPort;
    @JsonProperty("socks_port")
    private String socksPort;
    @JsonProperty("notebook_sg")
    private String notebookSg;
    @JsonProperty("notebook_profile")
    private String notebookProfile;
    @JsonProperty("notebook_subnet")
    private String notebookSubnet;
    @JsonProperty("edge_sg")
    private String edgeSG;

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getUserOwnBicketName() {
        return userOwnBicketName;
    }

    public String getTunnelPort() {
        return tunnelPort;
    }

    public String getSocksPort() {
        return socksPort;
    }

    public String getNotebookSg() {
        return notebookSg;
    }

    public String getNotebookProfile() {
        return notebookProfile;
    }

    public String getNotebookSubnet() {
        return notebookSubnet;
    }

    public String getEdgeSG() {
        return edgeSG;
    }
}
