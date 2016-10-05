package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAWSCredentialDTO {
    @JsonProperty
    protected String user;
    @JsonProperty("instance_name")
    protected String instanceName;
    @JsonProperty("ami_id")
    protected String amiId;
    @JsonProperty("edge_security_group_name")
    protected String edgeSecurityGroupName;
    @JsonProperty("key_name")
    protected String keyName;
    @JsonProperty
    protected String region;
    @JsonProperty("policy_arn")
    protected String policyArn;
    @JsonProperty("policy_name")
    protected String policyName;
    @JsonProperty("isolated_security_group_name")
    protected String isolatedSecurityGroupName;
    @JsonProperty("role_profile_name")
    protected String roleProfileName;
    @JsonProperty("bucket_name")
    protected String bucketName;
    @JsonProperty("public_subnet_id")
    protected String publicSubnetId;
    @JsonProperty("private_subnet_cidr")
    protected String privateSubnetCidr;
    @JsonProperty("vpc_id")
    protected String vpcId;
    @JsonProperty("service_base_name")
    protected String serviceBaseName;
    @JsonProperty("role_name")
    protected String roleName;
    @JsonProperty("instance_size")
    protected String instanceSize;

    @JsonProperty("socks_port")
    protected String socksPort;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


}
