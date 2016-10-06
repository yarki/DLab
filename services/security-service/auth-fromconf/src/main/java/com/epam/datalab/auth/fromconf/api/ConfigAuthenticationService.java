package com.epam.datalab.auth.fromconf.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.rest.AbstractAuthenticationService;
import com.epam.dlab.auth.rest.AuthorizedUsers;
import com.epam.dlab.auth.rest.DataLabAuthenticationConfig;

@Path("/")
public class ConfigAuthenticationService extends AbstractAuthenticationService<DataLabAuthenticationConfig> {

	private final Map<String, UserInfo> usersMap;

	public ConfigAuthenticationService(DataLabAuthenticationConfig config) {
		super(config);
		Map<String, UserInfo> usersMap = config.getUsers();
		if(usersMap != null) {
			this.usersMap = usersMap;
		} else {
			this.usersMap = new HashMap<>();
		}
	}
	
	@Override
	@GET
	@Path("/validate")
	@Produces(MediaType.TEXT_PLAIN)
	public String validate(
			@QueryParam("username") String username, 
			@QueryParam("password") String password,
			@QueryParam("access_token") String accessToken) {
		log.debug("validating username:{} password:{} token:{}",username,password,accessToken);
		if( this.isAccessTokenAvailable(accessToken) ) {
			return accessToken;
		} else {
			UserInfo ui = usersMap.get(username);
			if( ui != null ) {
				if(password.equals(ui.getAccessToken())) {
					UUID id = UUID.randomUUID();
					String newToken = id.toString();
					this.rememberUserInfo(newToken, ui);
					return newToken;
				}
			}
		}
		return "";
	}

	@Override
	@POST
	@Path("/login")
	@Produces(MediaType.TEXT_HTML)
	public String login(
			@FormParam("username") String username, 
			@FormParam("username") String password, 
			@FormParam("username") String destination ) {
		
		String token = validate(username, password, "");
		
		if( ! "".equals(token) ) {
			String redirectUrl = addAccessTokenToUrl(removeAccessTokenFromUrl( destination ), token);
			log.debug("Redirect {} to {}",username ,redirectUrl);
			return String.format(POST_LOGIN_REDIRECT_HEAD, redirectUrl);
		} else {
			log.debug("User not found. Redirect {} to login page",username,password);
			return String.format(POST_LOGIN_REDIRECT_HEAD, "/");
		}
	}

	@Override
	@GET
	@Path("/logout")
	//@Produces(MediaType.TEXT_HTML)
	public Response logout(String access_token) {
		this.forgetAccessToken(access_token);
		Response response = Response.seeOther(URI.create("/")).build();
		return response;
	}

	@Override
	@GET
	@Path("/user_info")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo getUserInfo(@QueryParam("access_token") String access_token) {
		UserInfo ui = AuthorizedUsers.getInstance().getUserInfo(access_token);
		log.debug("Authorized {} {}", access_token, ui);
		return ui;
	}

}
