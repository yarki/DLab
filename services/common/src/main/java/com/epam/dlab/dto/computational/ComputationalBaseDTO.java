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

import com.epam.dlab.dto.ResourceBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract public class ComputationalBaseDTO<T extends ComputationalBaseDTO<?>> extends ResourceBaseDTO<T> {
    @JsonProperty("edge_user_name")
    private String edgeUserName;

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public void setEdgeUserName(String edgeUserName) {
        this.edgeUserName = edgeUserName;
    }

    @SuppressWarnings("unchecked")
    public T withEdgeUserName(String edgeUserName) {
        setEdgeUserName(edgeUserName);
        return (T) this;
    }
}
