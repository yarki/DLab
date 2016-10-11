package com.epam.dlab.auth.script;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.epam.dlab.auth.UserInfo;

public class ScriptHolderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws ScriptException {
		ScriptHolder sh = new ScriptHolder();
		Map<String,Object> map = new HashMap<>();
		map.put("key1", "val1");
		map.put("key2", "val2");
		UserInfo ui1 = sh.evalOnce("first", "javascript", "var enrichUserInfo=function(ui,context){ui.addRole(context['key1']);ui.setFirstName(\"Mike\");return ui;}").apply(new UserInfo("",""), map);
		System.out.println(ui1);
		assertNotNull(ui1);

		UserInfo ui2 = sh.evalOnce("second", "python", "def enrichUserInfo(ui,context):\n   ui.addRole(context['key2'])\n   ui.setLastName(\"Teplitskiy\")\n   return ui\n").apply(ui1, map);
		System.out.println(ui2);
		assertNotNull(ui2);

	}

}
