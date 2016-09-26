/*
Copyright 2016 EPAM Systems, Inc.
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.epam.datalab.auth.fromconf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.epam.datalab.auth.DataLabAuthenticationConfig;
import com.epam.datalab.auth.api.ConfigurableResource;
import com.epam.datalab.auth.core.AuthorizedUsers;
import com.epam.datalab.auth.core.UserInfo;

@Path("/login")
@Produces(MediaType.TEXT_HTML)
@Timed
public class Authenticate extends ConfigurableResource<DataLabAuthenticationConfig> {
	
	private final Map<String, UserInfo> usersMap;
	
	public final static String URI_PARAMETER_PATTERN = "[&]?access_token=([^&]$|[^&]*)";
	public static String filterUrl(String url) {
		return url.replaceAll(URI_PARAMETER_PATTERN, "").replace("?&", "?").replaceFirst("\\?$", "");
	}
	
	public Authenticate(DataLabAuthenticationConfig conf) {
		super(conf);
		Map<String, UserInfo> usersMap = conf.getUsers();
		if(usersMap != null) {
			this.usersMap = usersMap;
		} else {
			this.usersMap = new HashMap<>();
		}
	}
	
	@POST
	public String doAuthentication(@FormParam("username") String username, @FormParam("password")String password, @FormParam("next")String next, @Context HttpServletRequest request ) {
		UserInfo ui = authenticate(username, password);
		if( ui != null ) {
			UUID id = UUID.randomUUID();
			String token = id.toString();
			UserInfo authenticated = ui.withToken(token);
			AuthorizedUsers.getInstance().addUserInfo(token, authenticated);
			String newToken = "access_token=" + token;
			next = filterUrl(next);
			StringBuilder redirectUrl = new StringBuilder(next);
			String referer = request.getHeader("referer");
			if( ! next.contains("?")) {
				redirectUrl.append("?");
			} else {
				redirectUrl.append("&");			
			}
			redirectUrl.append("access_token=").append(token);
			log.debug("Redirect {} to {}",username ,redirectUrl);
			return "<html><head><meta http-equiv=\"refresh\" content=\"0; url="+redirectUrl+"\" /></head></html>";
		} else {
			log.debug("User not found. Redirect {} to login page",username,password);
		}
		
		return "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /></head></html>";
	}
	
	public UserInfo authenticate(String username, String password) {
		if(usersMap.containsKey(username)) {
			UserInfo ui = usersMap.get(username);
			if(password.equals(ui.getAccessToken())) {
				return ui;
			} else {
				return null;
			}
		}
		return null;
	}
}
