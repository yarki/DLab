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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.datalab.auth.fromconf.api.ConfigAuthenticationService;
import com.epam.datalab.auth.fromconf.api.LoginService;
import com.epam.dlab.auth.client.DataLabAuthenticationConfig;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;


public class ConfigAuthenticationApp extends Application<DataLabAuthenticationConfig> {

	private final static Logger LOG = LoggerFactory.getLogger(ConfigAuthenticationApp.class);
	
	public static void main(String[] args) throws Exception {
		
		String[] params = null;
		
		if(args.length != 0 ) {
			params = args;
		} else {
			params = new String[] { "server", "config.yml" };
		}
		LOG.debug("Starting Config Authentication Service with params: {}",String.join(",", params));
		new ConfigAuthenticationApp().run(params);
	}

	@Override
	public void initialize(Bootstrap<DataLabAuthenticationConfig> bootstrap) {
		bootstrap.addBundle(new ViewBundle<DataLabAuthenticationConfig>());
		//bootstrap.addBundle(new AssetsBundle("/"));
	}

	@Override
	public void run(DataLabAuthenticationConfig conf, Environment env) throws Exception {
		env.jersey().register( new ConfigAuthenticationService(conf) );
		env.jersey().register( new LoginService(conf) );
	}

}
