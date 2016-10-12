package com.epam.dlab.auth.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.ldap.core.Request;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class SecurityServiceConfiguration extends Configuration {

	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	public SecurityServiceConfiguration() {
		super();
	}
	
	@JsonProperty
	private List<Request> ldapSearch;
	
	public List<Request> getLdapSearch() {
		return ldapSearch;
	}

	@JsonProperty
	private String ldapBindTemplate;
	
	@JsonProperty
	private Map<String,String> ldapConnectionConfig = new HashMap<String, String>();
	private LdapConnectionConfig _ldapConnectionConfig;
	
	public LdapConnectionConfig getLdapConnectionConfig() {
		if(_ldapConnectionConfig == null) {
			_ldapConnectionConfig = new LdapConnectionConfig();
			_ldapConnectionConfig.setLdapHost(ldapConnectionConfig.get("ldapHost"));
			_ldapConnectionConfig.setLdapPort(Integer.parseInt(ldapConnectionConfig.get("ldapPort")));
			_ldapConnectionConfig.setName(ldapConnectionConfig.get("name"));
			_ldapConnectionConfig.setCredentials(ldapConnectionConfig.get("credentials"));
			//TODO: add all configurable properties
			//      from the LdapConnectionConfig class
		}
		return _ldapConnectionConfig;
		
	}

	public String getLdapBindTemplate() {
		return ldapBindTemplate;
	}
	
	List<SearchRequest> searchRequestList = null;

	@JsonProperty
	private String defaultRedirectFromAuthentication;

	public String getDefaultRedirectFromAuthentication() {
		return defaultRedirectFromAuthentication;
	}

}
