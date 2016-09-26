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
package com.epam.datalab.auth.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.datalab.auth.DataLabAuthenticationConfig;
import com.epam.datalab.auth.api.Login;
import com.epam.datalab.auth.api.Logout;
import com.epam.datalab.auth.ldap.api.Authenticate;
import com.epam.datalab.auth.ldap.api.Authorize;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;


public class LdapAuthenticationApp extends Application<DataLabAuthenticationConfig> {

	private final static Logger LOG = LoggerFactory.getLogger(LdapAuthenticationApp.class);
	
	public static void main(String[] args) throws Exception {
		
		String[] params = null;
		
		if(args.length != 0 ) {
			params = args;
		} else {
			params = new String[] { "server", "config.yml" };
		}
		LOG.debug("Starting Config Authentication Service with params: {}",String.join(",", params));
		new LdapAuthenticationApp().run(params);
	}

	@Override
	public void initialize(Bootstrap<DataLabAuthenticationConfig> bootstrap) {
		bootstrap.addBundle(new ViewBundle<DataLabAuthenticationConfig>());
	}

	@Override
	public void run(DataLabAuthenticationConfig conf, Environment env) throws Exception {
		env.jersey().register( new Login(conf) );
		env.jersey().register( new Logout(conf) );
		env.jersey().register( new Authenticate(conf) );
		env.jersey().register( new Authorize(conf) );
	}

}
