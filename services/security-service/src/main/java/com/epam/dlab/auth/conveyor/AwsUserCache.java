package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.AssemblingConveyor;
import com.aegisql.conveyor.BuilderSupplier;
import com.aegisql.conveyor.cart.command.CancelCommand;
import com.aegisql.conveyor.utils.caching.CachingConveyor;
import com.aegisql.conveyor.utils.caching.ImmutableReference;
import com.amazonaws.services.identitymanagement.model.User;
import com.epam.dlab.auth.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
public class AwsUserCache extends CachingConveyor<String,String,User> {

    private final static Logger LOG = LoggerFactory.getLogger(AwsUserCache.class);

    private final static AwsUserCache INSTANCE = new AwsUserCache();

    public static AwsUserCache getInstance() {
        return INSTANCE;
    }

    private AwsUserCache() {
        super();
        this.setName("AwsUserInfoCache");
        this.setIdleHeartBeat(1, TimeUnit.SECONDS);
        this.setDefaultBuilderTimeout(10, TimeUnit.MINUTES);
        this.setDefaultCartConsumer((b,l,s)->{
            LOG.debug("AwsUserInfoCache consume {} {}",l,s.get());
        });
    }

    public void removeAwsUserInfo(String token) {
        this.addCommand(new CancelCommand<String>(token));
    }

    public User getAwsUserInfo(String token) {
        Supplier<? extends User> s = this.getProductSupplier(token);
        if( s == null ) {
            return null;
        } else {
            return s.get();
        }
    }

    public void save(User userInfo) {
        CompletableFuture<Boolean> cacheFuture = AwsUserCache.getInstance().createBuild(userInfo.getUserId(), new ImmutableReference<User>(userInfo));
        try {
            if(! cacheFuture.get() ) {
                throw new Exception("Cache offer future returned 'false' for "+userInfo);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cache offer failed for "+userInfo,e);
        }
    }

}
