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

abstract public class StatusEnvBaseDTO<T extends StatusEnvBaseDTO<?>> extends StatusBaseDTO<T> {
    @JsonProperty("instance_id")
    private String instanceId;
    @JsonProperty("exploratory_name")
    private String exploratoryName;
    @JsonProperty("exploratory_template_name")
    private String exploratoryTemplateName;
    
    @SuppressWarnings("unchecked")
	private final T self = (T)this;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public T withInstanceId(String instanceId) {
    	setInstanceId(instanceId);
        return self;
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

    public String getExploratoryTemplateName() {
        return exploratoryTemplateName;
    }

    public void setExploratoryTemplateName(String exploratoryTemplateName) {
        this.exploratoryTemplateName = exploratoryTemplateName;
    }

    public T withExploratoryTemplateName(String exploratoryTemplateName) {
        setExploratoryTemplateName(exploratoryTemplateName);
        return self;
    }

    @Override
    public ToStringHelper toStringHelper(Object self) {
    	return super.toStringHelper(self)
    	        .add("instanceId", instanceId)
    	        .add("exploratoryName", exploratoryName)
    	        .add("exploratoryTemplateName", exploratoryTemplateName);
    }
    
    @Override
    public String toString() {
    	return toStringHelper(this).toString();
    }
}
