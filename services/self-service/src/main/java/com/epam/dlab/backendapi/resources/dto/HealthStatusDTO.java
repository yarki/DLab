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

/** Stores the health statuses for services.
 */
public class HealthStatusDTO {
    @JsonProperty("mongo_alive")
    private boolean mongoAlive;
    @JsonProperty("provisioning_alive")
    private boolean provisioningAlive;

    /** Returns <b>true</b> if the Mongo database is available. */
    public boolean isMongoAlive() {
        return mongoAlive;
    }

    /** Sets the Mongo database availability. */
    public void setMongoAlive(boolean mongoAlive) {
        this.mongoAlive = mongoAlive;
    }

    /** Sets the Mongo database availability. */
    public HealthStatusDTO withMongoAlive(boolean mongoAlive) {
        setMongoAlive(mongoAlive);
        return this;
    }

    /** Returns <b>true</b> if the provisioning service is available. */
    public boolean isProvisioningAlive() {
        return provisioningAlive;
    }

    /** Sets the provisioning service availability. */
    public void setProvisioningAlive(boolean provisioningAlive) {
        this.provisioningAlive = provisioningAlive;
    }

    /** Sets the provisioning service availability. */
    public HealthStatusDTO withProvisioningAlive(boolean provisioningAlive) {
        setProvisioningAlive(provisioningAlive);
        return this;
    }
}
