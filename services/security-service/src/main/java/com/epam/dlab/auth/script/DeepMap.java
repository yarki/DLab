package com.epam.dlab.auth.script;

import java.util.HashMap;
import java.util.Map;

public class DeepMap {
	
	private final Map<String, Object> root;
	
	public DeepMap(Map<String, Object> parent) {
		super();
		this.root = parent;
	}

	public DeepMap() {
		super();
		this.root = new HashMap<>();
	}

	public Map<String, Object> getRoot() {
		return root;
	}
	
	public DeepMap getBranch(String branchName) {
		Map<String, Object> branch = (Map<String, Object>) root.get(branchName);
		if( branch == null ) {
			branch = new HashMap<>();
			root.put(branchName, branch);
		}
		return new DeepMap(branch);
	}
	
	public void put(String key,Object val) {
		root.put(key, val);
	}
	
}
