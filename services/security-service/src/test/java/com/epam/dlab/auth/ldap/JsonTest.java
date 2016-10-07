package com.epam.dlab.auth.ldap;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.epam.dlab.auth.UserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTest {

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
	public void test1() throws IOException {
		ObjectMapper om = new ObjectMapper();
		String jv = om.writeValueAsString(new UserInfo("user","info"));
		assertNotNull(jv);
		System.out.println(jv);
		UserInfo ui = om.readerFor(UserInfo.class).readValue(jv);
		assertNotNull(ui);
		System.out.println(ui);
		//String js = "{\\\"username\\\":\"user\",\\\"access_token\\\":\"info\",\\\"firstName\\\":null,\\\"lastName\\\":null,\\\"roles\\\":[]}";
		//System.out.println(js);
		//UserInfo ui2 = om.readerFor(UserInfo.class).readValue(js);
		
		
	}
	@Test
	public void test2() throws IOException {
		ObjectMapper om = new ObjectMapper();
		UserInfo ui = new UserInfo("user","info");
		ui.setFirstName("first");
		ui.setLastName("last");
		ui.addRole("r1");
		ui.addRole("r2");
		String jv = om.writeValueAsString(ui);
		assertNotNull(jv);
		System.out.println(jv);
		UserInfo ui2 = om.readerFor(UserInfo.class).readValue(jv);
		assertNotNull(ui2);
		System.out.println(ui2);
		//String js = "{\\\"username\\\":\"user\",\\\"access_token\\\":\"info\",\\\"firstName\\\":null,\\\"lastName\\\":null,\\\"roles\\\":[]}";
		//System.out.println(js);
		//UserInfo ui2 = om.readerFor(UserInfo.class).readValue(js);
		
		
	}

}
