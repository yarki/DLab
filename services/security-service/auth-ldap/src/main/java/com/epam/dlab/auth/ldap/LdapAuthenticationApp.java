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
package com.epam.dlab.auth.ldap;

import java.util.List;

import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.client.DataLabAuthenticationConfig;
import com.epam.dlab.auth.ldap.api.LdapAuthenticationService;
import com.epam.dlab.auth.ldap.api.LoginService;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;


public class LdapAuthenticationApp extends Application<LdapAuthenticationConfig> {

	private final static Logger LOG = LoggerFactory.getLogger(LdapAuthenticationApp.class);
	
	public static void main(String[] args) throws Exception {
		
		String[] params = null;
		
		if(args.length != 0 ) {
			params = args;
		} else {
			params = new String[] { "server", "config.yml" };
		}
		LOG.debug("Starting Config Authentication Service with params: {}",String.join(",", params));
		PythonInterpreter.initialize(System.getProperties(),System.getProperties(), new String[0]);
		new LdapAuthenticationApp().run(params);
	}

	@Override
	public void initialize(Bootstrap<LdapAuthenticationConfig> bootstrap) {
		bootstrap.addBundle(new ViewBundle<LdapAuthenticationConfig>());
	}

	@Override
	public void run(LdapAuthenticationConfig conf, Environment env) throws Exception {
		
		String ldapBindTemplate = conf.getLdapBindTemplate();
		LOG.debug("ldapBindTemplate {}",ldapBindTemplate);
		
		env.jersey().register( new LoginService(conf) );
		env.jersey().register( new LdapAuthenticationService(conf) );
//		env.jersey().register( new Authenticate(conf) );
//		env.jersey().register( new Authorize(conf) );
	}

}
