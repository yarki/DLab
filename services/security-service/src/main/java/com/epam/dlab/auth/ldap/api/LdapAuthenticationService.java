/***************************************************************************

 Copyright (c) 2016, EPAM SYSTEMS INC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 ****************************************************************************/

package com.epam.dlab.auth.ldap.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.ValidatingPoolableLdapConnectionFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.UserInfoDAO;
import com.epam.dlab.auth.ldap.SecurityServiceConfiguration;
import com.epam.dlab.auth.ldap.core.Request;
import com.epam.dlab.auth.ldap.core.ReturnableConnection;
import com.epam.dlab.auth.ldap.core.UserInfoDAODumbImpl;
import com.epam.dlab.auth.ldap.core.UserInfoDAOMongoImpl;
import com.epam.dlab.auth.ldap.core.filter.SearchResultProcessor;
import com.epam.dlab.auth.rest.AbstractAuthenticationService;
import com.epam.dlab.auth.rest.AuthorizedUsers;
import com.epam.dlab.auth.rest.ExpirableContainer;
import com.epam.dlab.auth.script.ScriptHolder;
import com.epam.dlab.auth.script.SearchResultToDictionaryMapper;
import com.epam.dlab.dto.UserCredentialDTO;

import io.dropwizard.setup.Environment;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LdapAuthenticationService extends AbstractAuthenticationService<SecurityServiceConfiguration> {

	private final LdapConnectionConfig connConfig;
	private final List<Request> requests;
	private final String bindTemplate;
	private final LdapConnectionPool usersPool;
	private final LdapConnectionPool searchPool;
	private final ExpirableContainer<Map<String, Object>> filteredDictionaries = new ExpirableContainer<>();
	private final ScriptHolder script = new ScriptHolder();

	private UserInfoDAO userInfoDao;
	
	public LdapAuthenticationService(SecurityServiceConfiguration config, Environment env) {
		super(config);
		this.connConfig = config.getLdapConnectionConfig();
		this.requests = config.getLdapSearch();
		this.bindTemplate = config.getLdapBindTemplate();
		PoolableObjectFactory<LdapConnection> userPoolFactory = new ValidatingPoolableLdapConnectionFactory(connConfig);
		this.usersPool = new LdapConnectionPool(userPoolFactory);
		PoolableObjectFactory<LdapConnection> searchPoolFactory = new ValidatingPoolableLdapConnectionFactory(
				connConfig);
		this.searchPool = new LdapConnectionPool(searchPoolFactory);
		AuthorizedUsers.setInactiveTimeout(config.getInactiveUserTimeoutMillSec());
		if(config.isUserInfoPersistenceEnabled()) {
			this.userInfoDao = new UserInfoDAOMongoImpl(config.getMongoFactory().build(env),config.getInactiveUserTimeoutMillSec());
		} else {
			this.userInfoDao = new UserInfoDAODumbImpl();
		}
	}

	@Override
	@POST
	@Path("/login")
	public Response login(UserCredentialDTO credential, @Context HttpServletRequest request) {
		String username    = credential.getUsername();
		String password    = credential.getPassword();
		String accessToken = credential.getAccessToken();
		String remoteIp    = request.getRemoteAddr();
		log.debug("validating username:{} password:****** token:{} ip:{}", username, accessToken,remoteIp);
		UserInfo ui;

		if (this.isAccessTokenAvailable(accessToken)) {
			return Response.ok(accessToken).build();
		} else {
			try (ReturnableConnection userRCon = new ReturnableConnection(usersPool)) {
				LdapConnection userCon = userRCon.getConnection();
				// just confirm user exists
				String bind = String.format(bindTemplate, username);
				userCon.bind(bind, password);
				userCon.unBind();
				ui = new UserInfo(username, "******");
				log.debug("user '{}' identified. fetching data...", username);
				try (ReturnableConnection searchRCon = new ReturnableConnection(searchPool)) {
					LdapConnection searchCon = searchRCon.getConnection();
					Map<String, Object> conextTree = new HashMap<>();
					for (Request req : requests) {
						if (req == null) {
							continue;
						}
						SearchResultProcessor proc = req.getSearchResultProcessor();
						SearchRequest sr = req.buildSearchRequest(new HashMap<String, Object>() {
							private static final long serialVersionUID = 1L;
							{
								put(Pattern.quote("${username}"), username);
							}
						});
						String filter = sr.getFilter().toString();
						Map<String, Object> contextMap = filteredDictionaries.get(filter);
						SearchResultToDictionaryMapper mapper = new SearchResultToDictionaryMapper(req.getName(),
								conextTree);
						if (contextMap == null) {
							log.debug("Retrieving new branch {} for {}", req.getName(), filter);
							try (SearchCursor cursor = searchCon.search(sr)) {
								contextMap = mapper.transformSearchResult(cursor);
							}
							if (req.isCache()) {
								filteredDictionaries.put(filter, contextMap, req.getExpirationTimeMsec());
							}
						} else {
							log.debug("Restoring old branch {} for {}: {}", req.getName(), filter, contextMap);
							mapper.getBranch().putAll(contextMap);
						}
						if (proc != null) {
							log.debug("Executing: {}", proc.getLanguage());
							ui = script.evalOnce(req.getName(), proc.getLanguage(), proc.getCode()).apply(ui,
									conextTree);
						}

					}
				}

			} catch (Exception e) {
				log.error("LDAP error", e);
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			ui.setRemoteIp(remoteIp);
			String token = getRandomToken();
			UserInfo finalUserInfo = rememberUserInfo(token, ui);
			userInfoDao.saveUserInfo(finalUserInfo);
			rememberUserInfo(token, ui);
			return Response.ok(token).build();
		}
	}

	@Override
	@POST
	@Path("/getuserinfo")
	public UserInfo getUserInfo(String access_token, @Context HttpServletRequest request) {
		String remoteIp = request.getRemoteAddr();
		UserInfo ui     = AuthorizedUsers.getInstance().getUserInfo(access_token);
		if(ui == null) {
			ui = userInfoDao.getUserInfoByAccessToken(access_token);
			if( ui != null ) {
				ui = rememberUserInfo(access_token, ui);
				userInfoDao.updateUserInfoTTL(access_token, ui);
				log.debug("restored UserInfo from DB {}",ui);
			}
		} else {
			log.debug("updating TTL {}",ui);
			userInfoDao.updateUserInfoTTL(access_token, ui);
		}
		log.debug("Authorized {} {} {}", access_token, ui, remoteIp);
		return ui;
	}

	@Override
	@POST
	@Path("/logout")
	public Response logout(String access_token) {
		UserInfo ui = this.forgetAccessToken(access_token);
		userInfoDao.deleteUserInfo(access_token);
		log.debug("Logged out {} {}", access_token, ui);
		return Response.ok().build();
	}
}
