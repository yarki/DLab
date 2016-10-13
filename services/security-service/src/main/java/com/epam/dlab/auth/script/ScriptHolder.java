package com.epam.dlab.auth.script;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.epam.dlab.auth.UserInfo;

public class ScriptHolder {
	
	private final static String FUNCTION    = "enrichUserInfo";
	
	private final ScriptEngineManager   mgr = new ScriptEngineManager();
	private final Map<String,Invocable> map = new HashMap<>();
	
	public ScriptHolder() {
		
	}
	
	public BiFunction<UserInfo,Map<String,?>,UserInfo> evalOnce(String name, String language, String code) throws ScriptException {
		if( ! map.containsKey(name)) {
			ScriptEngine engine = mgr.getEngineByName( language );
			engine.eval(code);
			map.put(name, (Invocable) engine);
		}
		return (ui,context)->{
			try {
				return (UserInfo) map.get(name).invokeFunction(FUNCTION, ui,context);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
	
	
}
