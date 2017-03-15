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

package com.epam.dlab.dto.keyload;

import com.epam.dlab.dto.StatusBaseDTO;
import com.epam.dlab.dto.edge.EdgeInfoDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects.ToStringHelper;

public class UploadFileResultDTO extends StatusBaseDTO<UploadFileResultDTO> {
    @JsonProperty
    private EdgeInfoDTO edgeInfo;

    public EdgeInfoDTO getEdgeInfo() {
        return edgeInfo;
    }

    public void setEdgeInfo(EdgeInfoDTO edgeInfo) {
        this.edgeInfo = edgeInfo;
    }

    public UploadFileResultDTO withEdgeInfo(EdgeInfoDTO edgeInfo) {
    	setEdgeInfo(edgeInfo);
    	return this;
    }
    
    @Override
    public ToStringHelper toStringHelper(Object self) {
    	return super.toStringHelper(self)
    			.add("edgeInfo", edgeInfo);
    }
    
    @Override
    public String toString() {
    	return toStringHelper(this).toString();
    }
}
