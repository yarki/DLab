package com.epam.dlab.auth.ldap.core.python;

import java.io.InputStream;

import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.core.UserInfo;
import com.epam.dlab.auth.core.UserInfoTools;
import com.epam.dlab.auth.ldap.core.filter.UserInfoEnrichment;

public class PythonUserInfoEnrichment implements UserInfoEnrichment<PyDictionary> {
	
	private final static Logger LOG = LoggerFactory.getLogger(PythonUserInfoEnrichment.class);
	private final String code;
	private final PythonInterpreter python = new PythonInterpreter();
	
	public PythonUserInfoEnrichment(String code) {
		this.code = code;
		python.exec("import json");
		python.exec(this.code);
		LOG.info("Initilalized {}",this);
	}

	public PythonUserInfoEnrichment(InputStream code) {
		this.code = "from stream";
		python.exec("import json");
		python.exec(this.code);
		LOG.info("Initilalized {}",this);
	}

	@Override
	public UserInfo enrichUserInfo(UserInfo ui, PyDictionary context) {
		String json = UserInfoTools.toJson(ui);
		python.set("ui", new PyString(json));
		python.set("context", context);
		python.exec("result = json.dumps(enrichUserInfo(json.loads(ui),context))");
		PyObject res = python.get("result");
		return UserInfoTools.toUserInfo(res.toString());
	}

	@Override
	public String toString() {
		return "PythonUserInfoEnrichment code:\n" + code + "\n";
	}

}
