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

package com.epam.dlab.backendapi.api.instance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserComputationalResourceDTO {
    @JsonProperty("computational_name")
    private String computationalName;
    @JsonProperty
    private String status;
    @JsonProperty("up_time_since")
    private String upTimeSince;
    @JsonProperty("master_node_shape")
    private String masterShape;
    @JsonProperty("slave_node_shape")
    private String slaveShape;
    @JsonProperty("slave_instance_number")
    private String slaveNumber;

    public String getComputationalName() {
        return computationalName;
    }

    public void setComputationalName(String computationalName) {
        this.computationalName = computationalName;
    }

    public UserComputationalResourceDTO withComputationalName(String computationalName) {
        setComputationalName(computationalName);
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserComputationalResourceDTO withStatus(String status) {
        setStatus(status);
        return this;
    }

    public String getUpTimeSince() {
        return upTimeSince;
    }

    public void setUpTimeSince(String upTimeSince) {
        this.upTimeSince = upTimeSince;
    }

    public UserComputationalResourceDTO withUpTimeSince(String upTimeSince) {
        setUpTimeSince(upTimeSince);
        return this;
    }

    public String getMasterShape() {
        return masterShape;
    }

    public void setMasterShape(String masterShape) {
        this.masterShape = masterShape;
    }

    public UserComputationalResourceDTO withMasterShape(String masterShape) {
        setMasterShape(masterShape);
        return this;
    }

    public String getSlaveShape() {
        return slaveShape;
    }

    public void setSlaveShape(String slaveShape) {
        this.slaveShape = slaveShape;
    }

    public UserComputationalResourceDTO withSlaveShape(String slaveShape) {
        setSlaveShape(slaveShape);
        return this;
    }

    public String getSlaveNumber() {
        return slaveNumber;
    }

    public void setSlaveNumber(String slaveNumber) {
        this.slaveNumber = slaveNumber;
    }

    public UserComputationalResourceDTO withSlaveNumber(String slaveNumber) {
        setSlaveNumber(slaveNumber);
        return this;
    }
}
