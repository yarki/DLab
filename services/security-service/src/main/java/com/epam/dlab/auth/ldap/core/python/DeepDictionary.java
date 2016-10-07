package com.epam.dlab.auth.ldap.core.python;

import org.python.core.PyDictionary;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;

public class DeepDictionary {
	/**
	 * 
	 */

	private final PyDictionary root;

	DeepDictionary(PyDictionary parent) {
		super();
		this.root = parent;
	}
	
	public DeepDictionary() {
		super();
		this.root = new PyDictionary();
	}
	
	public PyDictionary getPyDictionary() {
		return root;
	}
	
	public DeepDictionary getBranch(String branch) {
		PyObject d = root.get(new PyString(branch));
		if(d == null || d instanceof PyNone) {
			d = new PyDictionary();
			root.put(new PyString(branch), d);
		}
		return new DeepDictionary((PyDictionary) d);
	}
	
	public void put(Object key,Object val) {
		root.put(key, val);
	}
	
}
