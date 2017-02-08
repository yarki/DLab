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

import java.util.List;

/** Stores the health statuses for services.
 */
public class HealthStatusPageDTO {
    @JsonProperty("status")
    private HealthStatusEnum status;
    @JsonProperty("list_resources")
    private List<HealthStatusResourcePageDTO> listResources;

    public HealthStatusEnum getStatus() {
        return status;
    }

    public void setStatus(HealthStatusEnum status) {
        this.status = status;
    }

    public List<HealthStatusResourcePageDTO> getListResources() {
        return listResources;
    }

    public void setListResources(List<HealthStatusResourcePageDTO> listResources) {
        this.listResources = listResources;
    }

    public HealthStatusPageDTO withStatus(HealthStatusEnum status) {
        setStatus(status);
        return this;
    }

    public HealthStatusPageDTO withListResources(List<HealthStatusResourcePageDTO> listResources) {
        setListResources(listResources);
        return this;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("provisioningAlive", listResources)
                .toString();
    }



}