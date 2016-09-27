package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.LDAPUser;
import com.epam.dlab.backendapi.api.User;
import com.epam.dlab.backendapi.core.RESTService;
import com.epam.dlab.backendapi.core.SecurityAPI;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.epam.dlab.backendapi.dao.MongoService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.epam.dlab.backendapi.core.RESTServiceFactory.SECURITY_SERVICE;


/**
 * Created by Alexey Suprun
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource implements MongoCollections, SecurityAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    @Inject
    private MongoService mongoService;
    @Inject
    @Named(SECURITY_SERVICE)
    private RESTService securityService;

    @POST
    public LDAPUser login(@FormParam("login") String login, @FormParam("password") String password) {
        LOGGER.debug("Try login user = {}", login);
        User user = new User(login, password);
        mongoService.getCollection(LOGIN_ATTEMPT).insertOne(user.getDocument());
        return securityService.post(LOGIN, user, LDAPUser.class);
    }
}
