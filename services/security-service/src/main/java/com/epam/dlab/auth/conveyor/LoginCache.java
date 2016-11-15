package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.cart.command.CancelCommand;
import com.aegisql.conveyor.utils.caching.CachingConveyor;
import com.epam.dlab.auth.UserInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by Mikhail_Teplitskiy on 11/15/2016.
 */
public class LoginCache extends CachingConveyor<String,LoginStep,UserInfo> {

    private final static LoginCache INSTANCE = new LoginCache();

    public static LoginCache getInstance() {
        return INSTANCE;
    }

    private LoginCache() {
        super();
        this.setName("UserInfoCache");
        this.setIdleHeartBeat(1, TimeUnit.SECONDS);
        this.setDefaultBuilderTimeout(60, TimeUnit.MINUTES);
        this.enablePostponeExpiration(true);
        this.setExpirationPostponeTime(60,TimeUnit.MINUTES);
        this.setBuilderSupplier(UserInfoBuilder::new);
        this.acceptLabels(LoginStep.USER_INFO);
    }

    public void removeUserInfo(String token) {
        this.addCommand(new CancelCommand<String>(token));
    }

    public UserInfo getUserInfo(String token) {
        Supplier<? extends UserInfo> s = this.getProductSupplier(token);
        if( s == null ) {
            return null;
        } else {
            return s.get();
        }
    }

    public void save(UserInfo userInfo) {
        CompletableFuture<Boolean> cacheFuture = LoginCache.getInstance().offer(userInfo.getAccessToken(),userInfo,LoginStep.USER_INFO);
        try {
            if(! cacheFuture.get() ) {
                throw new Exception("Offer future returned 'false' for "+userInfo);
            }
        } catch (Exception e) {
            throw new RuntimeException("User Info cache offer failure for "+userInfo,e);
        }
    }

}
