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

package com.epam.dlab.dto.edge;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdgeCreateDTO extends EdgeBaseDTO<EdgeCreateDTO> {
    @JsonProperty("edge_vpc_id")
    private String vpcId;
    @JsonProperty("creds_subnet_id")
    private String subnetId;
    @JsonProperty("creds_iam_user")
    private String iamUser;
    @JsonProperty("creds_security_groups_ids")
    private String securityGroupIds;

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public EdgeCreateDTO withVpcId(String vpcId) {
        setVpcId(vpcId);
        return this;
    }

     public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public EdgeCreateDTO withSubnetId(String subnetId) {
        setSubnetId(subnetId);
        return this;
    }

    public String getIamUser() {
        return iamUser;
    }

    public void setIamUser(String iamUser) {
        this.iamUser = iamUser;
    }

    public EdgeCreateDTO withIamUser(String iamUser) {
        setIamUser(iamUser);
        return this;
    }

    public String getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(String securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    public EdgeCreateDTO withSecurityGroupIds(String securityGroupIds) {
        setSecurityGroupIds(securityGroupIds);
        return this;
    }

}
