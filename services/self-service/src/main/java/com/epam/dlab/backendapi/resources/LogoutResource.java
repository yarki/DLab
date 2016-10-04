package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.client.rest.SecurityAPI;
import com.epam.dlab.backendapi.dao.MongoCollections;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Alexey Suprun
 */
@Path("/logout")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
public class LogoutResource implements MongoCollections, SecurityAPI {
//    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);
//
//    @Inject
//    AuthenticationServiceConfig authenticationConfig;
//
//    @Inject
//    Client jerseyClient;
//
//    @GET
//    public String logout(@QueryParam("access_token") String token) {
//        LOGGER.debug("Try logout token = {}", token);
//        WebTarget wt = jerseyClient.target(authenticationConfig.getLogoutUrl()).queryParam("access_token", token);
//        Builder builder = wt.request();
//        builder.get();
//        String url = "/webapp/index.html?";
//        return String.format(POST_LOGIN_REDIRECT_HEAD, url);
//
//    }
}
