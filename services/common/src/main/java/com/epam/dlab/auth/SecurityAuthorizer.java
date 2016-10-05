package com.epam.dlab.auth;

import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexey Suprun
 */
public class SecurityAuthorizer implements Authorizer<UserInfo> {
    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityRestAuthenticator.class);

    @Override
    public boolean authorize(UserInfo userInfo, String role) {
        LOGGER.debug("authorize user = {} with role = {}", userInfo.getName(), role);
        return true;
    }
}
