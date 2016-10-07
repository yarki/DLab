package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.SecurityAPI;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.epam.dlab.backendapi.dao.SecurityDAO;
import com.epam.dlab.dto.UserCredentialDTO;
import com.epam.dlab.restclient.RESTService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.epam.dlab.auth.SecurityRestAuthenticator.SECURITY_SERVICE;

/**
 * Created by Alexey Suprun
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SecurityResource implements MongoCollections, SecurityAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityResource.class);

    @Inject
    private SecurityDAO dao;
    @Inject
    @Named(SECURITY_SERVICE)
    RESTService securityService;

    @Path("/login")
    @POST
    public String login(UserCredentialDTO credential) {
        LOGGER.debug("Try login user = {}", credential.getUsername());
        dao.writeLoginAttempt(credential);
        return securityService.post(LOGIN, credential, String.class);
    }

    @Path("/logout")
    @POST
    public Response logout(String accessToken) {
        LOGGER.debug("Try logout accessToken {}", accessToken);
        return securityService.post(LOGOUT, accessToken, Response.class);
    }
}