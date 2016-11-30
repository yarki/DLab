package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.BuilderSupplier;
import com.aegisql.conveyor.cart.command.CancelCommand;
import com.aegisql.conveyor.utils.caching.CachingConveyor;
import com.aegisql.conveyor.utils.caching.ImmutableReference;
import com.amazonaws.services.identitymanagement.model.User;
import com.epam.dlab.auth.UserInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Created by Mikhail_Teplitskiy on 11/15/2016.
 */
public class AwsUserCache extends CachingConveyor<String,String,User> {

    private final static AwsUserCache INSTANCE = new AwsUserCache();

    public static AwsUserCache getInstance() {
        return INSTANCE;
    }

    private AwsUserCache() {
        super();
        this.setName("AwsUserInfoCache");
        this.setIdleHeartBeat(1, TimeUnit.SECONDS);
        this.setDefaultBuilderTimeout(10, TimeUnit.MINUTES);
        this.enablePostponeExpiration(false);
        this.setExpirationPostponeTime(10,TimeUnit.MINUTES);
        this.acceptLabels("");
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
            cacheFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        try {
            if(! cacheFuture.get() ) {
                throw new Exception("Cache offer future returned 'false' for "+userInfo);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cache offer failed for "+userInfo,e);
        }
    }

}
