package com.epam.dlab.auth.ldap.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.ValidatingPoolableLdapConnectionFactory;
import org.python.core.PyDictionary;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.ldap.SecurityServiceConfiguration;
import com.epam.dlab.auth.ldap.core.Request;
import com.epam.dlab.auth.ldap.core.filter.SearchResultProcessor;
import com.epam.dlab.auth.ldap.core.python.DeepDictionary;
import com.epam.dlab.auth.ldap.core.python.PythonUserInfoEnrichment;
import com.epam.dlab.auth.ldap.core.python.SearchResultToDictionaryMapper;
import com.epam.dlab.auth.rest.AbstractAuthenticationService;
import com.epam.dlab.auth.rest.AuthorizedUsers;
import com.epam.dlab.dto.UserCredentialDTO;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LdapAuthenticationService extends AbstractAuthenticationService<SecurityServiceConfiguration> {

	private final LdapConnectionConfig connConfig;
	private final List<Request> requests;
	private final String bindTemplate;
	private final LdapConnectionPool usersPool;
	private final LdapConnectionPool searchPool;

	public LdapAuthenticationService(SecurityServiceConfiguration config) {
		super(config);
		this.connConfig = config.getLdapConnectionConfig();
		this.requests = config.getLdapSearch();
		this.bindTemplate = config.getLdapBindTemplate();
		PoolableObjectFactory<LdapConnection> userPoolFactory = new ValidatingPoolableLdapConnectionFactory(connConfig);
		this.usersPool = new LdapConnectionPool(userPoolFactory);
		PoolableObjectFactory<LdapConnection> searchPoolFactory = new ValidatingPoolableLdapConnectionFactory(
				connConfig);
		this.searchPool = new LdapConnectionPool(searchPoolFactory);
	}

	@Override
	@POST
	@Path("/login")
	public String login(UserCredentialDTO credential) {
		String username = credential.getUsername();
		String password = credential.getPassword();
		String accessToken = credential.getAccessToken();
		log.debug("validating username:{} password:{} token:{}", username, password, accessToken);
		UserInfo ui;

		if (this.isAccessTokenAvailable(accessToken)) {
			return accessToken;
		} else {
			String bind = String.format(bindTemplate, username);
			LdapConnection userCon = null;
			try {
				userCon = usersPool.borrowObject();
				// just confirm user exists
				userCon.bind(bind, password);
				userCon.unBind();
				ui = new UserInfo(username, "******");
				log.debug("user '{}' identified. fetching data...", username);
				DeepDictionary root = new DeepDictionary();
				LdapConnection searchCon = searchPool.borrowObject();
				PyDictionary conextDictionary = new PyDictionary();
				for(Request req: requests) {
					SearchCursor cursor = null;
					try {
						if (req == null) {
							continue;
						}
						SearchResultToDictionaryMapper mapper = new SearchResultToDictionaryMapper(req.getName(),conextDictionary);
						SearchResultProcessor proc = req.getSearchResultProcessor();
						SearchRequest sr = req.buildSearchRequest(new HashMap<String, Object>() {
							{
								put(Pattern.quote("${username}"), username);
							}
						});
						cursor = searchCon.search(sr);
						mapper.transformSearchResult(cursor);
						if (proc != null) {
							log.debug("Executing: {}", proc.getLanguage());
							PythonUserInfoEnrichment uie = new PythonUserInfoEnrichment(proc.getCode());
							ui = uie.enrichUserInfo(ui, conextDictionary);
							log.debug("Enriched UserInfo {}", ui);
						}
					} catch (IOException | LdapException e) {
						log.error("LDAP SEARCH error", e);
						throw new WebApplicationException(e);
					} finally {
						if (cursor != null) {
							try {
								cursor.close();
							} catch (IOException e) {
							}
						}
					}
				}
				searchPool.releaseConnection(searchCon);

			} catch (Exception e) {
				log.error("LDAP error", e);
				ui = null;
				throw new WebApplicationException(e);
			} finally {
				if (userCon != null) {
					try {
						usersPool.releaseConnection(userCon);
					} catch (Exception e) {
						log.error("LDAP POOL error", e);
						throw new WebApplicationException(e);
					}
				}
			}
			String token = getRandomToken();
			rememberUserInfo(token, ui);
			return token;
		}
	}

	@Override
	@POST
	@Path("/getuserinfo")
	public UserInfo getUserInfo(String access_token) {
		UserInfo ui = AuthorizedUsers.getInstance().getUserInfo(access_token);
		log.debug("Authorized {} {}", access_token, ui);
		return ui;
	}

	@Override
	@POST
	@Path("/logout")
	public Response logout(String access_token) {
		this.forgetAccessToken(access_token);
		return Response.ok().build();
	}
}
