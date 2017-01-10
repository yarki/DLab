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
import com.epam.dlab.dto.exploratory.ExploratoryStatusDTO;
import com.epam.dlab.rest.client.RESTService;
import com.fasterxml.jackson.databind.JsonNode;

import static com.epam.dlab.rest.contracts.ApiCallbacks.EXPLORATORY;
import static com.epam.dlab.rest.contracts.ApiCallbacks.STATUS_URI;

public class ExploratoryCallbackHandler extends ResourceCallbackHandler<ExploratoryStatusDTO> {
    private static final String EXPLORATORY_ID_FIELD = "notebook_name";
    private static final String EXPLORATORY_URL_FIELD = "exploratory_url";
    private static final String EXPLORATORY_USER_FIELD = "exploratory_user";
    private static final String EXPLORATORY_PASSWORD_FIELD = "exploratory_pass";

    private final String exploratoryName;
    private final String uuid;
    
    @Override
    public String getUUID() {
    	return uuid;
    }

    public ExploratoryCallbackHandler(RESTService selfService, DockerAction action, String originalUuid, String user, String exploratoryName, String accessToken) {
        super(selfService, user, accessToken, originalUuid, action);
        this.uuid = originalUuid;
        this.exploratoryName = exploratoryName;
    }

    protected String getCallbackURI() {
        return EXPLORATORY + STATUS_URI;
    }

    protected ExploratoryStatusDTO parseOutResponse(JsonNode resultNode, ExploratoryStatusDTO baseStatus) {
        return baseStatus
                .withExploratoryId(getTextValue(resultNode.get(EXPLORATORY_ID_FIELD)))
                .withExploratoryUrl(getTextValue(resultNode.get(EXPLORATORY_URL_FIELD)))
                .withExploratoryUser(getTextValue(resultNode.get(EXPLORATORY_USER_FIELD)))
                .withExploratoryPassword(getTextValue(resultNode.get(EXPLORATORY_PASSWORD_FIELD)));
    }

    protected ExploratoryStatusDTO getBaseStatusDTO(UserInstanceStatus status) {
        return super.getBaseStatusDTO(status).withExploratoryName(exploratoryName);
    }
}
