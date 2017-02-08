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
import com.google.common.base.MoreObjects;

/** Stores the health statuses for services.
 */
public class HealthStatusResourcePageDTO {
    @JsonProperty("type")
    private String type;
    @JsonProperty("resource_id")
    private String resourceId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("status")

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public HealthStatusResourcePageDTO withStatus(String Status) {
        setStatus(Status);
        return this;
    }

    public HealthStatusResourcePageDTO withType(String type) {
        setType(type);
        return this;
    }

    public HealthStatusResourcePageDTO withResourceId(String resourceId) {
        setResourceId(resourceId);
        return this;
    }


    @Override
    public String toString() {
    	return MoreObjects.toStringHelper(this)
    			.add("type", type)
    			.add("resourceId", resourceId)
                .add("status",status)
    	        .toString();
    }


}
