package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.core.AuthenticationServiceConfig;
import com.epam.dlab.backendapi.client.rest.SecurityAPI;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;

import static com.epam.dlab.auth.rest_api.AbstractAuthenticationService.POST_LOGIN_REDIRECT_HEAD;

/**
 * Created by Alexey Suprun
 */
@Path("/logout")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
public class LogoutResource implements MongoCollections, SecurityAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    @Inject
    AuthenticationServiceConfig authenticationConfig;

    @Inject
    Client jerseyClient;

//    @GET
//    public String logout(@QueryParam("access_token") String token) {
//        LOGGER.debug("Try logout token = {}", token);
//        WebTarget wt = jerseyClient.target(authenticationConfig.getLogoutUrl()).queryParam("access_token", token);
//        Link.Builder builder = wt.request();
//        builder.get();
//        String url = "/webapp/index.html?";
//        return String.format(POST_LOGIN_REDIRECT_HEAD, url);
//    }
}
