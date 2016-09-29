package com.epam.dlab.backendapi.auth;

import com.epam.dlab.backendapi.api.User;
import io.dropwizard.auth.Authorizer;

/**
 * Created by Alexey Suprun
 */
public class SelfServiceAuthorizer implements Authorizer<User> {
    @Override
    public boolean authorize(User user, String s) {
        return true;
    }
}
