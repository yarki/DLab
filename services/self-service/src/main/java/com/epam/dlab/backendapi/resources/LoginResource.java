package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.User;
import com.epam.dlab.backendapi.client.rest.SecurityAPI;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.util.Collections;

/**
 * Created by Alexey Suprun
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource implements MongoCollections, SecurityAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);


//    @Inject
//    Client jerseyClient;

    @POST
    public User login(@FormParam("username") String username, @FormParam("password") String password) {
//    	Objects.requireNonNull(authenticationConfig);
//    	Objects.requireNonNull(jerseyClient);
        LOGGER.debug("Try login user = {}", username);
//        WebTarget wt = jerseyClient.target(authenticationConfig.getAccessTokenUrl());
//        wt = wt.queryParam("username", username).queryParam("password", password);
//        Builder builder = wt.request(MediaType.TEXT_PLAIN);
//        String token = builder.get(String.class);
//        String url = addAccessTokenToUrl(next, token);
        return new User("admin", "admin", Collections.singletonList("admin group"));
    }
}
