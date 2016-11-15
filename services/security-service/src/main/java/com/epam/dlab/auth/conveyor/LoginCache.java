package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.cart.command.CancelCommand;
import com.aegisql.conveyor.utils.caching.CachingConveyor;
import com.epam.dlab.auth.UserInfo;

import java.util.concurrent.TimeUnit;

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

    public void cancel(String token) {
        this.addCommand(new CancelCommand<String>(token));
    }

    public UserInfo getUserInfo(String token) {
        return this.getProductSupplier(token).get();
    }

}
