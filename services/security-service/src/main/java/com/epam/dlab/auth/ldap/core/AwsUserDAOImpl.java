package com.epam.dlab.auth.ldap.core;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
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
