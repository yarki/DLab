package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.SecurityAPI;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.epam.dlab.backendapi.dao.SecurityDAO;
import com.epam.dlab.dto.UserCredentialDTO;
import com.epam.dlab.restclient.RESTService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.epam.dlab.auth.SecurityRestAuthenticator.SECURITY_SERVICE;

/**
 * Created by Alexey Suprun
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SecurityResource implements MongoCollections, SecurityAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityResource.class);

    @Inject
    private SecurityDAO dao;
    @Inject
    @Named(SECURITY_SERVICE)
    RESTService authenticationService;

    @Path("/login")
    @POST
    public Optional<UserInfo> login(UserCredentialDTO credential) {
        LOGGER.debug("Try login user = {}", credential.getUsername());
        dao.writeLoginAttempt(credential);
        return authenticationService.post(LOGIN, credential, Optional.class);
    }

    @Path("/logout")
    @POST
    public Response login(@Auth UserInfo userInfo) {
        LOGGER.debug("Try logout user = {}", userInfo.getName());
        return Response.ok().build();
    }
}