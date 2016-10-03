package com.epam.dlab.auth.ldap.core;

import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

public class Request {
/*  
-   request: 
    scope: SUBTREE
    attributes: 
      - "*"
    timeLimit: 0
    base: dc=example,dc=com
    filter: 
 * */
	
	private String scope;
	private List<String> attributes;
	private int timeLimit = 0;
	private String base;
	private String filter = "";
	public String getScope() {
		return scope;
	}
	public String[] getAttributes() {
		return attributes.toArray(new String[]{});
	}
	public int getTimeLimit() {
		return timeLimit;
	}
	public String getBase() {
		return base;
	}
	public String getFilter() {
		return filter;
	}
	
	public SearchRequest buildSearchRequest(Map<String,Object> replace) {
		SearchRequest sr = new SearchRequestImpl();
		try {
			sr.setBase(new Dn(this.base));
			sr.addAttributes(this.getAttributes());
			if(this.filter != null && ! "".equals(this.filter ) ){
				String f = filter;
				for(String key:replace.keySet()) {
					f = f.replaceAll(key, replace.get(key).toString());
				}
				sr.setFilter(f);				
			}
			sr.setScope(SearchScope.valueOf(this.scope));
			sr.setTimeLimit(this.timeLimit);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return sr;
	}
	
	@Override
	public String toString() {
		return "RequestConfig [scope=" + scope + ", attributes=" + attributes + ", timeLimit=" + timeLimit + ", base=" + base
				+ ", filter=" + filter + "]";
	}


	
}
