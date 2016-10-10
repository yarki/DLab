package com.epam.dlab.auth.ldap.core.filter;

import org.apache.directory.api.ldap.model.cursor.SearchCursor;

import java.io.IOException;
import java.util.Map;

public interface SearchResultMapper<M extends Map<String,Object>> {
    public M transformSearchResult(SearchCursor cursor) throws IOException;
    public M getBranch();
}
