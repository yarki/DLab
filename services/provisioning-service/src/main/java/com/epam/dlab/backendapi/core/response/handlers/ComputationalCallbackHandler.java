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

package com.epam.dlab.backendapi.core.response.handlers;

import com.epam.dlab.UserInstanceStatus;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.dto.computational.ComputationalBaseDTO;
import com.epam.dlab.dto.computational.ComputationalCreateDTO;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.fasterxml.jackson.databind.JsonNode;

import static com.epam.dlab.rest.contracts.ApiCallbacks.COMPUTATIONAL;
import static com.epam.dlab.rest.contracts.ApiCallbacks.STATUS_URI;

public class ComputationalCallbackHandler extends ResourceCallbackHandler<ComputationalStatusDTO> {
    private static final String INSTANCE_ID_FIELD = "instance_id";
    private static final String COMPUTATIONAL_ID_FIELD = "hostname";
    
    private final ComputationalBaseDTO<?> dto;

    public ComputationalCallbackHandler(RESTService selfService, DockerAction action, String uuid, ComputationalBaseDTO<?> dto) {
        super(selfService, dto.getAwsIamUser(), uuid, action);
        this.dto = dto;
    }
    
    protected ComputationalBaseDTO<?> getDto() {
    	return dto;
    }
    
    @Override
    protected String getCallbackURI() {
        return COMPUTATIONAL + STATUS_URI;
    }

    @Override
    protected ComputationalStatusDTO parseOutResponse(JsonNode resultNode, ComputationalStatusDTO baseStatus) throws DlabException {
    	if (getAction() == DockerAction.CONFIGURE) {
    		baseStatus.withExploratoryName(getDto().getExploratoryName());
    	}
    	if (resultNode == null) {
    		return baseStatus;
    	}

    	switch (getAction()) {
    	case CREATE:
    		baseStatus
	            .withInstanceId(getTextValue(resultNode.get(INSTANCE_ID_FIELD)))
    			.withComputationalId(getTextValue(resultNode.get(COMPUTATIONAL_ID_FIELD)));
    		if (UserInstanceStatus.of(baseStatus.getStatus()) == UserInstanceStatus.RUNNING) {
    			baseStatus.withStatus(UserInstanceStatus.CONFIGURING);
    			ComputationalConfigure.configure(getUUID(), (ComputationalCreateDTO)getDto());
    		}
    		break;
		case CONFIGURE:
			baseStatus
				.withComputationalName(getDto().getComputationalName())
				.withUptime(null);
			break;
		default:
			break;
    	}
        return baseStatus;
    }
    
    @Override
    protected ComputationalStatusDTO getBaseStatusDTO(UserInstanceStatus status) {
        return super.getBaseStatusDTO(status)
        		.withExploratoryName(dto.getExploratoryName())
        		.withComputationalName(dto.getComputationalName());
    }

}

