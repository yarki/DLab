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
package com.epam.dlab.auth.example;

import javax.ws.rs.client.Client;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.SecurityRestAuthenticator;
import com.epam.dlab.auth.SecurityUnauthorizedHandler;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.example.api.TestService;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class ClientExampleApp extends Application<ExampleConfiguration> {

	private final static Logger LOG = LoggerFactory.getLogger(ClientExampleApp.class);
	
	public static void main(String[] args) throws Exception {
		String[] params = null;
		
		if(args.length != 0 ) {
			params = args;
		} else {
			params = new String[] { "server", "config.yml" };
		}
		LOG.debug("Starting Test Authentication Service with params: {}",String.join(",", params));
		new ClientExampleApp().run(params);
	}
	
	@Override
	public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
		bootstrap.addBundle(new ViewBundle<ExampleConfiguration>());
	}
	
	@Override
	public void run(ExampleConfiguration conf, Environment env) throws Exception {
	    final Client client = new JerseyClientBuilder(env).using(conf.getJerseyClientConfiguration())
                .build(getName());
		env.jersey().register(new TestService( conf ));
		env.jersey().register(new AuthDynamicFeature(
		        new OAuthCredentialAuthFilter.Builder<UserInfo>()
		            .setAuthenticator(new SecurityRestAuthenticator())
		            .setAuthorizer(new Authorizer<UserInfo>(){
						@Override
						public boolean authorize(UserInfo principal, String role) {
							LOG.debug("user:{} role:{}",principal,role );
							return true;
						}})
		            .setPrefix("Bearer")
		            .setUnauthorizedHandler(new SecurityUnauthorizedHandler())
		            .buildAuthFilter()));

		env.jersey().register(RolesAllowedDynamicFeature.class);
		env.jersey().register(new AuthValueFactoryProvider.Binder<>(UserInfo.class));

	}

}
