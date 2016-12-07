package com.epam.dlab.auth.core;

import com.aegisql.conveyor.BuilderSupplier;
import com.aegisql.conveyor.Testing;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.epam.dlab.auth.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/*
Copyright 2016 EPAM Systems, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
public class UserInfoBuilder implements Supplier<UserInfo>, Testing {

    private final static Logger LOG = LoggerFactory.getLogger(UserInfoBuilder.class);

    private String token;
    private String username;
    private UserInfo userInfo;

    private int readinessStatus = 0b00000000;

    public final static int FIRST_NAME      = 0b000001;
    public final static int LAST_NAME       = 0b000010;
    public final static int AWS_USER_SET    = 0b000100;
    public final static int ROLE_SET        = 0b001000;
    public final static int REMOTE_IP       = 0b010000;
    public final static int AWS_KEYS        = 0b100000;

    public final static int READYNESS_MASK  = 0b111111;

    public static boolean testMask(Supplier<? extends UserInfo> supplier, int mask) {
        UserInfoBuilder builder = (UserInfoBuilder) supplier;
        LOG.debug("testing {} vs {} = {}",builder.readinessStatus,mask,(builder.readinessStatus & mask) == mask);
        return (builder.readinessStatus & mask) == mask;
    }

    public static BuilderSupplier<UserInfo> supplier(final String token, final String username ) {
        LOG.debug("supplier requested {} {}",token, username);
        return () -> new UserInfoBuilder(token,username);
    }

    public static void firstName(UserInfoBuilder b, String firstName) {
        LOG.debug("firstName {}",firstName);

        b.userInfo.setFirstName(firstName);
        b.readinessStatus |= FIRST_NAME;
    }

    public static void lastName(UserInfoBuilder b, String lastName) {
        LOG.debug("lastName {}",lastName);

        b.userInfo.setLastName(lastName);
        b.readinessStatus |= LAST_NAME;
    }

    public static void remoteIp(UserInfoBuilder b, String remoteIp) {
        LOG.debug("remoteIp {}",remoteIp);

        b.userInfo.setRemoteIp(remoteIp);
        b.readinessStatus |= REMOTE_IP;
    }

    public static void awsUser(UserInfoBuilder b, Boolean awsUser) {
        LOG.debug("awsUser {}",awsUser);

        b.userInfo.setAwsUser(awsUser);
        b.readinessStatus |= AWS_USER_SET;
    }

    public static void roles(UserInfoBuilder b, Collection<String> roles) {
        LOG.debug("roles {}",roles);
        roles.forEach( role -> b.userInfo.addRole(role) );
        b.readinessStatus |= ROLE_SET;
    }

    public static void ldapUserInfo(UserInfoBuilder b, UserInfo ui) {
        LOG.debug("merge user info{}",ui);
        UserInfoBuilder.firstName(b,ui.getFirstName());
        UserInfoBuilder.lastName(b,ui.getLastName());
        UserInfoBuilder.roles(b,ui.getRoles());
    }

    public UserInfoBuilder(String token, String username) {
        this.token    = token;
        this.username = username;
        this.userInfo = new UserInfo(username,token);
    }

    public UserInfoBuilder() {

    }

    @Override
    public UserInfo get() {
        return userInfo;
    }

    @Override
    public String toString() {
        return "UserInfoBuilder{" +
                "userInfo=" + userInfo +
                ", readinessStatus=" + readinessStatus +
                '}';
    }

    @Override
    public boolean test() {
        return UserInfoBuilder.testMask(this,UserInfoBuilder.READYNESS_MASK);
    }

    public static void cloneUserInfo(UserInfoBuilder b, UserInfo ui) {
        b.userInfo = ui.withToken(ui.getAccessToken());
        b.username = ui.getName();
        b.token    = ui.getAccessToken();
    }

    public static void awsKeys(UserInfoBuilder userInfoBuilder, List<AccessKeyMetadata> keyMetadata) {
        LOG.debug("AWS Keys {}",keyMetadata);
        LongAdder counter = new LongAdder();
        if(keyMetadata != null) {
            keyMetadata.forEach(k -> {
                String key = k.getAccessKeyId();
                String status = k.getStatus();
                if ("Active".equalsIgnoreCase(status)) {
                    counter.increment();
                }
                userInfoBuilder.userInfo.addKey(key, status);
            });
        }
        if( counter.intValue() == 0 ) {
            throw new RuntimeException("Please contact AWS administrator to activate your Access Key");
        }
        userInfoBuilder.readinessStatus |= AWS_KEYS;
    }

    public static void failed(UserInfoBuilder userInfoBuilder, RuntimeException error) {
        LOG.error("UserInfo error {}", error.getMessage());
        throw error;
    }
}
