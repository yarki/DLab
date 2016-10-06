package com.epam.dlab.auth.ldap.core.python;

import java.io.IOException;

import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.python.core.PyDictionary;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.ldap.core.filter.SearchResultMapper;

public class SearchResultToDictionaryMapper implements SearchResultMapper<PyDictionary> {
	
	private final static Logger LOG = LoggerFactory.getLogger(SearchResultToDictionaryMapper.class);
	
	private final DeepDictionary root;
	private final DeepDictionary reqBranch;
	private final String name;
	
	public SearchResultToDictionaryMapper(String name) {
		this.name = name;
		this.root = new DeepDictionary();
		reqBranch = root.getBranch(name);
	}
	
	public SearchResultToDictionaryMapper(String name, PyDictionary context) {
		this.name = name;
		this.root = new DeepDictionary(context);
		reqBranch = root.getBranch(name);
	}
	
	@Override
	public PyDictionary transformSearchResult(SearchCursor cursor) throws IOException {
		LOG.debug(name);
		cursor.forEach(response -> {
			if (response instanceof SearchResultEntry) {
				Entry resultEntry = ((SearchResultEntry) response).getEntry();
				String dn = resultEntry.getDn().toString();
				LOG.debug("\tEntryDN {}",dn);
				DeepDictionary dnBranch = reqBranch.getBranch(dn);
				resultEntry.forEach(attr -> {
					dnBranch.put(new PyString(attr.getId() + ""), new PyString(attr.get() + ""));
					LOG.debug("\t\tAttr {}",attr);
				});
			}
		});
		cursor.close();
		return root.getPyDictionary();
	}

	@Override
	public String toString() {
		return "SearchResultToDictionaryMapper [name=" + name + ", root=" + root + "]";
	}
	
}
