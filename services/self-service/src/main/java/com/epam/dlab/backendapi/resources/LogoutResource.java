package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.AuthenticationConfiguration;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

/**
 * Created by Alexey Suprun
 */
@Path("/logout")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
public class LogoutResource implements MongoCollections {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    @Inject
    AuthenticationConfiguration authenticationConfig;

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
