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

package com.epam.dlab.backendapi.resources.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

/** Stores limits for creation of the computational resources.
 */
public class ComputationalLimitsDTO {
    @NotBlank
    @JsonProperty("max_emr_instance_count")
    private int maxEmrInstanceCount;

    /** Returns the maximum number of slave EMR instances than could be created. */
    public int getMaxEmrInstanceCount() {
    	return maxEmrInstanceCount;
    }
    
    /** Sets the maximum number of EMR instances than could be created. */
    public void setMaxEmrInstanceCount(int maxEmrInstanceCount) {
    	this.maxEmrInstanceCount = maxEmrInstanceCount;
    }
    
    /** Sets the maximum number of EMR instances than could be created. */
    public ComputationalLimitsDTO withMaxEmrInstanceCount(int maxEmrInstanceCount) {
    	this.maxEmrInstanceCount = maxEmrInstanceCount;
    	return this;
    }
}
