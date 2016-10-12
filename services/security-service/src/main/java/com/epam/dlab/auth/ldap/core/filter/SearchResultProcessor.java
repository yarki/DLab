package com.epam.dlab.auth.ldap.core.filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SearchResultProcessor {
	private String language;
	private String code;
	private String path;
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCode() {
		if(code == null || "".equals(code)) {
			try {
				code = new String(Files.readAllBytes(Paths.get(path)));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	@Override
	public String toString() {
		return "SearchResultProcessor [language=" + language + ", path=" + path + ", code=" + code + "]";
	}

	
	
	
}
