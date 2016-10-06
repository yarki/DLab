package com.epam.dlab.auth.ldap.core.filter;

import com.epam.dlab.auth.UserInfo;

import java.util.Map;

public interface UserInfoEnrichment<M extends Map> {
    public UserInfo enrichUserInfo(UserInfo ui, M context);
}
