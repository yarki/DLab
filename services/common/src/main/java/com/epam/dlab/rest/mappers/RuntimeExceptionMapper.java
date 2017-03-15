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

package com.epam.dlab.rest.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionMapper extends GenericExceptionMapper<RuntimeException> {
    final static Logger LOGGER = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof WebApplicationException) {
            return handleWebApplicationException(exception);
        }
        return super.toResponse(exception);
    }

    private Response handleWebApplicationException(RuntimeException exception) {
        WebApplicationException webAppException = (WebApplicationException) exception;

        if (webAppException.getResponse().getStatusInfo() == Response.Status.UNAUTHORIZED) {
            return web(exception, Response.Status.UNAUTHORIZED);
        }
        if (webAppException.getResponse().getStatusInfo() == Response.Status.NOT_FOUND) {
            return web(exception, Response.Status.NOT_FOUND);
        }

        return super.toResponse(exception);
    }

    private Response web(RuntimeException exception, Response.StatusType status) {
        LOGGER.error("Web application exception: {}", exception.getMessage(), exception);
        return Response.status(status).build();
    }
}
