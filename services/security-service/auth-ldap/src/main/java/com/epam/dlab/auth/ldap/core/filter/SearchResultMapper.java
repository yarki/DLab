package com.epam.dlab.auth.ldap.core.filter;

import java.io.IOException;
import java.util.Map;

import org.apache.directory.api.ldap.model.cursor.SearchCursor;

public interface SearchResultMapper<M extends Map<?,?>> {
	public M transformSearchResult(SearchCursor cursor) throws IOException;
}
