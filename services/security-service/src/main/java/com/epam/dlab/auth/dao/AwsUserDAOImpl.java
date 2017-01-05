/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.auth.dao;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.epam.dlab.auth.dao.filter.AwsUserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AwsUserDAOImpl implements AwsUserDAO {

    private final static Logger LOG = LoggerFactory.getLogger(AwsUserDAOImpl.class);

    private volatile AmazonIdentityManagement aim;

    public AwsUserDAOImpl(AWSCredentials credentials) {
        this.aim = new AmazonIdentityManagementClient(credentials);
    }

    @Override
    public User getAwsUser(String username) {
        User u = fetchAwsUser(username);
        LOG.debug("Fetched AWS user {}",u);
        return u;
    }

    @Override
    public void updateCredentials(AWSCredentials credentials) {
        this.aim         = new AmazonIdentityManagementClient(credentials);
    }

    @Override
    public List<AccessKeyMetadata> getAwsAccessKeys(String username) {
        List<AccessKeyMetadata> data = null;
        try {
            ListAccessKeysRequest request = new ListAccessKeysRequest().withUserName(username);
            ListAccessKeysResult result   = aim.listAccessKeys(request);
            data = result.getAccessKeyMetadata();
        } catch (Exception e) {
            LOG.error("AccessKeyMetadata for {} request failed: {}",username,e.getMessage());
        }
        return data;
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
