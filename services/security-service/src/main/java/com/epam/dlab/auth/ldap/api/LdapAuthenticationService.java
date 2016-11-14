/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.auth.ldap.api;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.identitymanagement.model.User;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.UserInfoDAO;
import com.epam.dlab.auth.conveyor.LoginConveyor;
import com.epam.dlab.auth.conveyor.LoginStep;
import com.epam.dlab.auth.ldap.SecurityServiceConfiguration;
import com.epam.dlab.auth.ldap.core.AwsUserDAOImpl;
import com.epam.dlab.auth.ldap.core.LdapUserDAO;
import com.epam.dlab.auth.ldap.core.UserInfoDAODumbImpl;
import com.epam.dlab.auth.ldap.core.UserInfoDAOMongoImpl;
import com.epam.dlab.auth.ldap.core.filter.AwsUserDAO;
import com.epam.dlab.auth.rest.AbstractAuthenticationService;
import com.epam.dlab.auth.rest.AuthorizedUsers;
import com.epam.dlab.dto.UserCredentialDTO;
import io.dropwizard.setup.Environment;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.*;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LdapAuthenticationService extends AbstractAuthenticationService<SecurityServiceConfiguration> {

	private final LdapUserDAO ldapUserDAO;
	private final AwsUserDAO awsUserDAO;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final ExecutorService threadpool = Executors.newFixedThreadPool(32);
	private UserInfoDAO userInfoDao;

	private final LoginConveyor loginConveyor = new LoginConveyor();

	public LdapAuthenticationService(SecurityServiceConfiguration config, Environment env) {
		super(config);
		AuthorizedUsers.setInactiveTimeout(config.getInactiveUserTimeoutMillSec());
		if(config.isUserInfoPersistenceEnabled()) {
			this.userInfoDao = new UserInfoDAOMongoImpl(config.getMongoFactory().build(env),config.getInactiveUserTimeoutMillSec());
		} else {
			this.userInfoDao = new UserInfoDAODumbImpl();
		}
		loginConveyor.setUserInfoDao(userInfoDao);
		if(config.isAwsUserIdentificationEnabled()) {
			DefaultAWSCredentialsProviderChain providerChain = new DefaultAWSCredentialsProviderChain();
			awsUserDAO = new AwsUserDAOImpl(providerChain.getCredentials());
			scheduler.scheduleAtFixedRate(()->{
				try {
					providerChain.refresh();
					awsUserDAO.updateCredentials(providerChain.getCredentials());
					log.debug("provider credentials refreshed");
				} catch (Exception e) {
					log.error("AWS provider error",e);
					throw e;
				}
			},5,5, TimeUnit.MINUTES);
		} else {
			awsUserDAO = null;
		}
		this.ldapUserDAO = new LdapUserDAO(config);
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
		String token = getRandomToken();
		UserInfo ui;
		if (this.isAccessTokenAvailable(accessToken)) {
			return Response.ok(accessToken).build();
		} else {
			CompletableFuture<UserInfo> uiFuture = loginConveyor.startUserInfoBuild(token,username);
			loginConveyor.add(token,remoteIp, LoginStep.REMOTE_IP);

			//Try to login
			threadpool.submit(()->{
				try {
					ldapUserDAO.getUserInfo(username,password);
					log.debug("User Authenticated: {}",username);
				} catch (Exception e) {
					loginConveyor.cancel(token);
				}
			});
			//Extract User Info from LDAP
			threadpool.submit(()->{
				try {
					UserInfo rolesUserInfo = ldapUserDAO.enrichUserInfo(new UserInfo(username, token));
					loginConveyor.add(token,rolesUserInfo,LoginStep.LDAP_USER_INFO);
				} catch (Exception e) {
					loginConveyor.cancel(token);
				}
			});
			//Check AWS account
			threadpool.submit(()->{
				if(config.isAwsUserIdentificationEnabled()) {
					try {
						User awsUser = awsUserDAO.getAwsUser(username);
						if (awsUser != null) {
							loginConveyor.add(token, true, LoginStep.AWS_USER);
						} else {
							loginConveyor.add(token, false, LoginStep.AWS_USER);
							log.warn("AWS User '{}' was not found. ", username);
						}
					} catch (Exception e) {
						loginConveyor.cancel(token);
					}
				} else {
					loginConveyor.add(token,false,LoginStep.AWS_USER);
				}
			});

			try {
				UserInfo userInfo = uiFuture.get(10,TimeUnit.SECONDS);
				log.debug("user info collected by conveyor '{}' ", userInfo);
			} catch (Exception e) {
				log.error("Conveyor error {}", e.getMessage());
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
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
