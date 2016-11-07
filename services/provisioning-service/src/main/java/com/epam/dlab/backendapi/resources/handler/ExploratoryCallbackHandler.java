/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.resources.handler;

import com.epam.dlab.backendapi.core.docker.command.DockerAction;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.constants.UserInstanceStatus;
import com.epam.dlab.dto.exploratory.ExploratoryStatusDTO;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

import static com.epam.dlab.registry.ApiCallbacks.EXPLORATORY;
import static com.epam.dlab.registry.ApiCallbacks.STATUS_URI;

public class ExploratoryCallbackHandler extends ResourceCallbackHandler<ExploratoryStatusDTO> {
    private static final String EXPLORATORY_ID_FIELD = "notebook_name";
    private static final String EXPLORATORY_URL_FIELD = "exploratory_url";

    private String exploratoryName;

    @SuppressWarnings("unchecked")
    public ExploratoryCallbackHandler(RESTService selfService, DockerAction action, String originalUuid, String user, String exploratoryName) {
        super(selfService, user, originalUuid, action);
        this.exploratoryName = exploratoryName;
    }

    protected String getCallbackURI() {
        return EXPLORATORY+STATUS_URI;
    }

    protected ExploratoryStatusDTO parseOutResponse(JsonNode resultNode, ExploratoryStatusDTO baseStatus) {
        JsonNode url = resultNode.get(EXPLORATORY_URL_FIELD);
        return baseStatus
                .withExploratoryId(resultNode.get(EXPLORATORY_ID_FIELD).textValue())
                .withExploratoryUrl(url != null ? url.textValue() : null);
    }

    protected ExploratoryStatusDTO getBaseStatusDTO(UserInstanceStatus status) {
        return super.getBaseStatusDTO(status).withExploratoryName(exploratoryName);
    }
}
