package com.epam.dlab.dto.awscredential;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class SecurityGroupRule {
    @JsonProperty("IpProtocol")
    private String ipProtocol;
}
