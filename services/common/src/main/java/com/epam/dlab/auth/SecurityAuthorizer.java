package com.epam.dlab.auth;

import com.epam.dlab.dto.UserInfo;
import io.dropwizard.auth.Authorizer;

/**
 * Created by Alexey Suprun
 */
public class SecurityAuthorizer implements Authorizer<UserInfo> {
    @Override
    public boolean authorize(UserInfo userInfo, String s) {
        return true;
    }
}
