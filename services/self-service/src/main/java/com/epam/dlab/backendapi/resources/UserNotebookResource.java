/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.UserNotebookDAO;
import com.google.inject.Inject;
import com.mongodb.client.result.DeleteResult;
import io.dropwizard.auth.Auth;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/usernotebook")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserNotebookResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUploaderResource.class);

    @Inject
    private UserNotebookDAO dao;

    @GET
    public Iterable<Document> getNotebooks(@Auth UserInfo userInfo) {
        LOGGER.debug("loading notebooks for user {}", userInfo.getName());
        return dao.find(userInfo.getName());
    }

    @GET
    @Path("/shape")
    public Iterable<Document> getShapes(@Auth UserInfo userInfo) {
        LOGGER.debug("loading shapes for user {}", userInfo.getName());
        return dao.findShapes();
    }

    @POST
    @Path("/{image}")
    public Response insertNotebook(@Auth UserInfo userInfo, @PathParam("image") String image) {
        LOGGER.debug("insert notebook {} for user {}", image, userInfo.getName());
        dao.insert(userInfo.getName(), image);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public DeleteResult deleteNotebook(@Auth UserInfo userInfo, @PathParam("id") String id) {
        LOGGER.debug("delete notebook with id {} for user {} ", id, userInfo.getName());
        return dao.delete(id);
    }
}
