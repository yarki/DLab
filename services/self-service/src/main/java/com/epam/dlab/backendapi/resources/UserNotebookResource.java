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

/**
 * Created by Alexey Suprun
 */
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
