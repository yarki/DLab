package com.epam.dlab.auth.ldap.core.filter;

import java.util.Map;

import com.epam.dlab.auth.core.UserInfo;

public interface UserInfoEnrichment<M extends Map<?,?>> {
	public UserInfo enrichUserInfo(UserInfo ui, M context);
}
