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

import com.epam.dlab.dto.StatusEnvBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects.ToStringHelper;

public class ComputationalStatusDTO extends StatusEnvBaseDTO<ComputationalStatusDTO> {
    @JsonProperty("computational_id")
    private String computationalId;
    @JsonProperty("computational_name")
    private String computationalName;
    
    public String getComputationalId() {
        return computationalId;
    }

    public void setComputationalId(String computationalId) {
        this.computationalId = computationalId;
    }

    public ComputationalStatusDTO withComputationalId(String computationalId) {
        setComputationalId(computationalId);
        return this;
    }

    public String getComputationalName() {
        return computationalName;
    }

    public void setComputationalName(String computationalName) {
        this.computationalName = computationalName;
    }

    public ComputationalStatusDTO withComputationalName(String computationalName) {
        setComputationalName(computationalName);
        return this;
    }

    @Override
    public ToStringHelper toStringHelper(Object self) {
    	return super.toStringHelper(self)
    	        .add("computationalId", computationalId)
    	        .add("computationalName", computationalName);
    }
    
    @Override
    public String toString() {
    	return toStringHelper(this).toString();
    }
}
