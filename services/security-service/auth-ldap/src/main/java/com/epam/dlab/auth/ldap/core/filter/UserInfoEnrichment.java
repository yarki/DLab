package com.epam.dlab.auth.ldap.core.filter;

import java.util.Map;

import com.epam.dlab.auth.UserInfo;

public interface UserInfoEnrichment<PyDictionary> {
	public UserInfo enrichUserInfo(UserInfo ui, PyDictionary context);
}
