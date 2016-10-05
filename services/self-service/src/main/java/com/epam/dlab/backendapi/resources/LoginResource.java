package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.AuthenticationConfiguration;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Alexey Suprun
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
public class LoginResource implements MongoCollections {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    @Inject
    AuthenticationConfiguration authenticationConfig;

//    @POST
//    public String login(@FormParam("username") String username, @FormParam("password") String password) {
//        LOGGER.debug("Try login user = {}", username);
//        WebTarget wt = jerseyClient.target(authenticationConfig.getAccessTokenUrl());
//        wt = wt.queryParam("username", username).queryParam("password", password);
//        Builder builder = wt.request(MediaType.TEXT_PLAIN);
//        String token = builder.get(String.class);
//        String url = addAccessTokenToUrl(next, token);
//        return String.format(POST_LOGIN_REDIRECT_HEAD, url);
//
//    }
}
