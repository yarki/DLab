package com.epam.dlab.auth.ldap.core.filter;

import java.util.Map;

import com.epam.dlab.auth.core.UserInfo;

public interface UserInfoEnrichment {
	public UserInfo enrichUserInfo(UserInfo ui, Map<String,Object> context);
	public UserInfo initiateUserInfo(Map<String,Object> context);
}
