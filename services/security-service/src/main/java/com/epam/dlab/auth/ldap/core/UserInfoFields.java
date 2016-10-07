package com.epam.dlab.auth.ldap.core;

import java.util.Set;

public interface UserInfoFields {
	String getUserName();
	String getAccessToken();
	Set<String> getRoles();
	String getFirstName();
	String getLastName();
}
