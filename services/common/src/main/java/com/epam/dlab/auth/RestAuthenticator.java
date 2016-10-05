package com.epam.dlab.auth;

import com.epam.dlab.dto.UserInfo;
import com.epam.dlab.restclient.RESTService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RestAuthenticator implements Authenticator<String, UserInfo> {
    public static final String AUTHENTICATION_CONFIGURATION = "authenticationConfiguration";

    private final static Logger LOGGER = LoggerFactory.getLogger(RestAuthenticator.class);

    @Inject
    @Named(AUTHENTICATION_CONFIGURATION)
    private RESTService authenticationService;

    @Override
    public Optional<UserInfo> authenticate(String credentials) throws AuthenticationException {
        LOGGER.debug("Authenticate token {}", credentials);
        return authenticationService.getBuilder("login")
                .header("access_token", credentials)
                .get(Optional.class);
    }
}
