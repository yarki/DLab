package com.epam.dlab.auth.rest_api;

import javax.ws.rs.core.Response;

import com.epam.dlab.auth.client.ConfigurableResource;
import com.epam.dlab.auth.core.AuthorizedUsers;
import com.epam.dlab.auth.core.UserInfo;

import io.dropwizard.Configuration;

public abstract class AbstractAuthenticationService<C extends Configuration> extends ConfigurableResource<C> {

	public final static String POST_LOGIN_REDIRECT_HEAD = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=%s\" /></head></html>";
	
	public final static String ACCESS_TOKEN_PARAMETER_PATTERN = "[&]?access_token=([^&]$|[^&]*)";

	public static String removeAccessTokenFromUrl(String url) {
		return url.replaceAll(ACCESS_TOKEN_PARAMETER_PATTERN, "").replace("?&", "?").replaceFirst("\\?$", "");
	}
	
	public static String addAccessTokenToUrl(String url, String token) {
		StringBuilder sb = new StringBuilder(url);
		if( ! url.contains("?")) {
			sb.append("?");
		} else {
			sb.append("&");			
		}
		sb.append("access_token=").append(token);
		return sb.toString();
	}
	
	public AbstractAuthenticationService(C config) {
		super(config);
	}

	public abstract String validate(String username, String password, String access_token);
	public abstract String login(String username, String password, String destination);
	public abstract Response logout(String access_token);
	public abstract UserInfo getUserInfo(String access_token);
	
	public void forgetAccessToken(String token) {
		AuthorizedUsers.getInstance().removeUserInfo(token);
	}
	
	public void rememberUserInfo(String token, UserInfo user) {
		AuthorizedUsers.getInstance().addUserInfo(token, user.withToken(token));
	}
	
	public boolean isAccessTokenAvailable(String token) {
		UserInfo ui = AuthorizedUsers.getInstance().getUserInfo(token);
		if( ui != null) {
			return true;
		} else {
			return false;
		}
	}
	
}
