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
    @JsonProperty("aws_vpc_id")
    private String awsVpcId;
    @JsonProperty("aws_subnet_id")
    private String awsSubnetId;
    @JsonProperty("aws_iam_user")
    private String iamUser;
    @JsonProperty("aws_security_groups_ids")
    private String awsSecurityGroupIds;

    public String getAwsVpcId() {
        return awsVpcId;
    }

    public void setAwsVpcId(String awsVpcId) {
    	
        this.awsVpcId = awsVpcId;
    }

    public EdgeCreateDTO withAwsVpcId(String awsVpcId) {
        setAwsVpcId(awsVpcId);
        return this;
    }

     public String getAwsSubnetId() {
        return awsSubnetId;
    }

    public void setAwsSubnetId(String awsSubnetId) {
        this.awsSubnetId = awsSubnetId;
    }

    public EdgeCreateDTO withAwsSubnetId(String awsSubnetId) {
        setAwsSubnetId(awsSubnetId);
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

    public String getAwsSecurityGroupIds() {
        return awsSecurityGroupIds;
    }

    public void setAwsSecurityGroupIds(String awsSecurityGroupIds) {
        this.awsSecurityGroupIds = awsSecurityGroupIds;
    }

    public EdgeCreateDTO withAwsSecurityGroupIds(String awsSecurityGroupIds) {
        setAwsSecurityGroupIds(awsSecurityGroupIds);
        return this;
    }

}
