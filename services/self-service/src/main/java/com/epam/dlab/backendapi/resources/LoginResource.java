package com.epam.dlab.backendapi.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.core.AuthenticationServiceConfig;
import com.epam.dlab.backendapi.client.rest.SecurityAPI;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.google.inject.Inject;
import static com.epam.dlab.auth.rest_api.AbstractAuthenticationService.POST_LOGIN_REDIRECT_HEAD;
import static com.epam.dlab.auth.rest_api.AbstractAuthenticationService.addAccessTokenToUrl;
import java.util.Objects;

/**
 * Created by Alexey Suprun
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
public class LoginResource implements MongoCollections, SecurityAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

	
	@Inject
	AuthenticationServiceConfig authenticationConfig;
	
	@Inject
	Client jerseyClient;

    @POST
    public String login(@FormParam("username") String username, @FormParam("password") String password,@FormParam("next") String next) {
    	Objects.requireNonNull(authenticationConfig);
    	Objects.requireNonNull(jerseyClient);
        LOGGER.debug("Try login user = {}", username);       
        WebTarget wt = jerseyClient.target(authenticationConfig.getAccessTokenUrl());
        wt = wt.queryParam("username", username).queryParam("password", password);
        Builder builder = wt.request(MediaType.TEXT_PLAIN);
        String token = builder.get(String.class);
        String url = addAccessTokenToUrl(next, token);
		return String.format(POST_LOGIN_REDIRECT_HEAD, url); 

    }
}
