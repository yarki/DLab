package com.epam.dlab.auth;

import io.dropwizard.auth.UnauthorizedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.net.URI;

public class RestAuthenticatorFailureHandler implements UnauthorizedHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestAuthenticatorFailureHandler.class);

    private final AuthenticationConfiguration authenticationConfiguration;

    public RestAuthenticatorFailureHandler(AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Override
    public Response buildResponse(String prefix, String realm) {
        String redirect = authenticationConfiguration.getLoginUrl();
        LOGGER.debug("Authentication failure redirect to {}", redirect);
        return Response.seeOther(URI.create(redirect)).build();
    }

}
