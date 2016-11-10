package com.epam.dlab.auth.conveyor;

import com.epam.dlab.auth.UserInfo;

import java.util.function.Supplier;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public class UserInfoBuilder implements Supplier<UserInfo> {

    private final String token;
    private final String username;
    private final UserInfo userInfo;


    public UserInfoBuilder(String token, String username) {
        this.token    = token;
        this.username = username;
        this.userInfo = new UserInfo(token,username);
    }

    @Override
    public UserInfo get() {
        return userInfo;
    }



}
