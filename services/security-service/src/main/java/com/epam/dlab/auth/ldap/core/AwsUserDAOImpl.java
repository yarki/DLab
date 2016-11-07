/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/
package com.epam.dlab.auth.ldap.core;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.epam.dlab.auth.ldap.core.filter.AwsUserDAO;
import com.epam.dlab.auth.rest.ExpirableContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Mikhail_Teplitskiy on 11/3/2016.
 */
public class AwsUserDAOImpl implements AwsUserDAO {

    private final static Logger LOG = LoggerFactory.getLogger(AwsUserDAOImpl.class);

    private final ExpirableContainer<User> usersCache = new ExpirableContainer<>();
    private final AWSCredentials credentials;
    private final AmazonIdentityManagement aim;

    public AwsUserDAOImpl(AWSCredentials credentials) {

        this.credentials = credentials;
        this.aim = new AmazonIdentityManagementClient(credentials);
        try {
            ListUsersResult lur = aim.listUsers();
            lur.getUsers().forEach(u -> {
                usersCache.put(u.getUserName(), u, 3600000);
                LOG.debug("Initialized AWS user {}",u);
            });

        } catch(Exception e) {
            LOG.error("Failed AWS user initialization. Will keep trying ... ",e);
        }
    }

    @Override
    public User getAwsUser(String username) {
        User u = usersCache.get(username);
        if(u == null) {
            u = fetchAwsUser(username);
            usersCache.put(username,u,600000);
            LOG.debug("Fetched AWS user {}",u);
        }
        return u;
    }

    private User fetchAwsUser(String username) {
        User user = null;
        try {
            GetUserRequest r = new GetUserRequest().withUserName(username);
            GetUserResult ur = aim.getUser(r);
            user = ur.getUser();
        } catch (NoSuchEntityException e) {
            LOG.error("User {} not found: {}",username,e.getMessage());
        }
        return user;
    }
}
