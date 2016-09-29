package com.epam.dlab.backendapi.auth;

import com.epam.dlab.backendapi.api.User;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.dao.LoginDAO;
import com.epam.dlab.backendapi.dao.MongoCollections;
import com.epam.dlab.backendapi.resources.DockerResource;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SECURITY_SERVICE;
import static com.epam.dlab.backendapi.client.rest.SecurityAPI.LOGIN;

/**
 * Created by Alexey Suprun
 */
public class SelfServiceAuthenticator implements Authenticator<BasicCredentials, User>, MongoCollections {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @Inject
    private LoginDAO dao;
    @Inject
    @Named(SECURITY_SERVICE)
    private RESTService securityService;

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        LOGGER.debug("Try login user = {}", credentials.getUsername());
        dao.loginAttempt(credentials);
        return securityService.post(LOGIN, credentials, Optional.class);
    }
}
