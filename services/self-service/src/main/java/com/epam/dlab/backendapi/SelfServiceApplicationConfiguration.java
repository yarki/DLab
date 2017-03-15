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

package com.epam.dlab.backendapi;

import javax.validation.Valid;

import com.epam.dlab.ServiceConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.util.Duration;

/** Configuration for Self Service.
 */
public class SelfServiceApplicationConfiguration extends ServiceConfiguration {

    @Valid
    @JsonProperty
    private boolean mocked;
    
    @Valid
    @JsonProperty
    private int minEmrInstanceCount;

    @Valid
    @JsonProperty
    private int maxEmrInstanceCount;
    
    @Valid
    @JsonProperty
    private Duration checkEnvStatusTimeout = Duration.minutes(10);


    /** Returns <b>true</b> if service is a mock. */
    public boolean isMocked() {
        return mocked;
    }
    
    /** Returns the minimum number of slave EMR instances than could be created. */
    public int getMinEmrInstanceCount() {
    	return minEmrInstanceCount;
    }

    /** Returns the maximum number of slave EMR instances than could be created. */
    public int getMaxEmrInstanceCount() {
    	return maxEmrInstanceCount;
    }
    
    /** Returns the timeout for check the status of environment via provisioning service. */
    public Duration getCheckEnvStatusTimeout() {
    	return checkEnvStatusTimeout;
    }
    
    public SelfServiceApplicationConfiguration withCheckEnvStatusTimeout(Duration checkEnvStatusTimeout) {
    	this.checkEnvStatusTimeout = checkEnvStatusTimeout;
    	return this;
    }
}
