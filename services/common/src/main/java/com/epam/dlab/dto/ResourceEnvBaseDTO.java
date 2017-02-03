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

package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects.ToStringHelper;

abstract public class ResourceEnvBaseDTO<T extends ResourceEnvBaseDTO<?>> extends ResourceBaseDTO<T> {
    @JsonProperty("conf_service_base_name")
    private String serviceBaseName;
    @JsonProperty("exploratory_name")
    private String exploratoryName;
    @JsonProperty("edge_user_name")
    private String edgeUserName;
    @JsonProperty("conf_os_user")
    private String confOsUser;
    @JsonProperty("conf_os_family")
    private String confOsFamily;
    @JsonProperty("application")
    private String applicationName;

    @SuppressWarnings("unchecked")
	private final T self = (T)this;

    public String getServiceBaseName() {
        return serviceBaseName;
    }

    public void setServiceBaseName(String serviceBaseName) {
        this.serviceBaseName = serviceBaseName;
    }

    public T withServiceBaseName(String serviceBaseName) {
        setServiceBaseName(serviceBaseName);
        return self;
    }

    public T withAwsRegion(String region) {
        setAwsRegion(region);
        return self;
    }

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public String getExploratoryName() {
        return exploratoryName;
    }

    public void setExploratoryName(String exploratoryName) {
        this.exploratoryName = exploratoryName;
    }

    public T withExploratoryName(String exploratoryName) {
        setExploratoryName(exploratoryName);
        return self;
    }

    public void setEdgeUserName(String edgeUserName) {
        this.edgeUserName = edgeUserName;
    }

    public T withEdgeUserName(String edgeUserName) {
        setEdgeUserName(edgeUserName);
        return self;
    }

    public String getConfOsUser() {
        return confOsUser;
    }

    public void setConfOsUser(String confOsUser) {
        this.confOsUser = confOsUser;
    }

    public T withConfOsUser(String confOsUser) {
        setConfOsUser(confOsUser);
        return self;
    }
    
    public String getConfOsFamily() {
        return confOsFamily;
    }

    public void setConfOsFamily(String confOsFamily) {
        this.confOsFamily = confOsFamily;
    }

    public T withConfOsFamily(String confOsFamily) {
        setConfOsFamily(confOsFamily);
        return self;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public T withApplicationName(String applicationName) {
        setApplicationName(applicationName);
        return self;
    }
    
    @Override
    public ToStringHelper toStringHelper(Object self) {
    	return super.toStringHelper(self)
    	        .add("serviceBaseName", serviceBaseName)
    	        .add("applicationName", applicationName)
    	        .add("exploratoryName", exploratoryName)
    	        .add("edgeUserName", edgeUserName)
    	        .add("confOsUser", confOsUser)
    	        .add("confOsFamily", confOsFamily);
    }
    
    @Override
    public String toString() {
    	return toStringHelper(this).toString();
    }
}
