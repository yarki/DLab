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
	
	private List<SearchRequest> getSearchRequests() {
		if( searchRequestList == null ) {
		final List<SearchRequest> list = new ArrayList<>();
		ldapSearch.forEach(req->{
			LOG.debug("request {}",req);
			if(req == null) {
				return;
			}
			SearchRequest sr = new SearchRequestImpl();
			try {
				sr.setBase(new Dn(req.getBase()));
				sr.addAttributes(req.getAttributes());
				if(req.getFilter() != null && ! "".equals(req.getFilter() ) ){
					sr.setFilter(req.getFilter());				
				}
				sr.setScope(SearchScope.valueOf(req.getScope()));
				sr.setTimeLimit(req.getTimeLimit());
				list.add(sr);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
		});
		searchRequestList = list;
		}
		return searchRequestList;
	}

	public String getDefaultRedirectFromAuthentication() {
		return defaultRedirectFromAuthentication;
	}

}
