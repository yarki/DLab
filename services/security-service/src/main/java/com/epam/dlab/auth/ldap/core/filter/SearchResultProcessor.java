package com.epam.dlab.auth.ldap.core.filter;

public class SearchResultProcessor {
	private String language;
	private String code;
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return "Filter [language=" + language + ", code=" + code + "]";
	}
	
	
	
}
