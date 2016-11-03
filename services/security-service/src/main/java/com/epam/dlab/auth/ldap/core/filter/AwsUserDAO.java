package com.epam.dlab.auth.ldap.core.filter;

import com.amazonaws.services.identitymanagement.model.User;

/**
 * Created by Mikhail_Teplitskiy on 11/3/2016.
 */
public interface AwsUserDAO {

    public User getAwsUser(String username);

}
