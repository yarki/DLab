/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.resources.handler;

import com.epam.dlab.backendapi.core.docker.command.DockerAction;
import com.epam.dlab.backendapi.core.response.folderlistener.FileHandlerCallback;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.constants.UserInstanceStatus;
import com.epam.dlab.dto.StatusBaseDTO;
import com.epam.dlab.exceptions.DlabException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

abstract public class ResourceCallbackHandler<T extends StatusBaseDTO> implements FileHandlerCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCallbackHandler.class);
    private ObjectMapper MAPPER = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

    private static final String STATUS_FIELD = "status";
    private static final String RESPONSE_NODE = "response";
    private static final String RESULT_NODE = "result";
    protected static final String USER_EXPLORATORY_NAME_FIELD = "exploratory_name";

    private static final String OK_STATUS = "ok";
    private static final String ERROR_STATUR = "err";

    private RESTService selfService;
    private String user;
    private String originalUuid;
    private DockerAction action;
    private Class<T> resultType;

    @SuppressWarnings("unchecked")
    public ResourceCallbackHandler(RESTService selfService, String user, String originalUuid, DockerAction action) {
        this.selfService = selfService;
        this.user = user;
        this.originalUuid = originalUuid;
        this.action = action;
        this.resultType = (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public boolean checkUUID(String uuid) {
        return originalUuid.equals(uuid);
    }

    @Override
    public boolean handle(String fileName, byte[] content) throws Exception {
        LOGGER.debug("get file {} actually waited for {}", fileName, originalUuid);
        JsonNode document = MAPPER.readTree(content);
        boolean success = isSuccess(document);
        UserInstanceStatus status = calcStatus(action, success);
        @SuppressWarnings("unchecked")
        T result = (T) resultType.newInstance().withUser(user).withStatus(status.getStatus());
        if (success) {
            JsonNode resultNode = document.get(RESPONSE_NODE).get(RESULT_NODE);
            result = parseOutResponse(resultNode, result);
        } else {
            LOGGER.error("Could not {} resource for user: {}, request: {}, docker response: {}", action, user, originalUuid, new String(content));
        }
        selfService.post(getCallbackURI(), result, resultType);
        return !UserInstanceStatus.FAILED.equals(status);
    }

    @Override
    public void handleError() {
        try {
            selfService.post(getCallbackURI(), resultType.newInstance().withUser(user).withStatus(UserInstanceStatus.FAILED.getStatus()), resultType);
        } catch (Throwable t) {
            throw new DlabException("Could not send status update for request " + originalUuid + ", user " + user, t);
        }
    }

    abstract protected String getCallbackURI();
    abstract protected T parseOutResponse(JsonNode document, T statusResult);

    private boolean isSuccess(JsonNode document) {
        return OK_STATUS.equals(document.get(STATUS_FIELD).textValue());
    }

    private UserInstanceStatus calcStatus(DockerAction action, boolean success) {
        if (success) {
            switch (action) {
                case CREATE: return UserInstanceStatus.CREATED;
                case START: return UserInstanceStatus.RUNNING;
                case STOP: return UserInstanceStatus.STOPPED;
                case TERMINATE: return UserInstanceStatus.TERMINATED;
            }
        }
        return UserInstanceStatus.FAILED;
    }
}
