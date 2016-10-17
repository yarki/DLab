package com.epam.dlab.auth;

import com.epam.dlab.client.restclient.RESTService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SecurityRestAuthenticator implements Authenticator<String, UserInfo>, SecurityAPI {
    public static final String SECURITY_SERVICE = "securityService";
    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityRestAuthenticator.class);

    @Inject
    @Named(SECURITY_SERVICE)
    private RESTService securityService;

    @Override
    public Optional<UserInfo> authenticate(String credentials) throws AuthenticationException {
        LOGGER.debug("authenticate token {}", credentials);
        return Optional.ofNullable(securityService.post(GET_USER_INFO, credentials, UserInfo.class));
    }
}
