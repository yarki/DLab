package com.epam.datalab.auth;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.epam.datalab.auth.core.AuthenticationService;

public class AuthenticationServiceTest {

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
	public void test1() {
		AuthenticationService as = new AuthenticationService();
		System.out.println(as.getAuthenticationUrl());
		assertEquals("http://localhost/login?",as.getAuthenticationUrl());
		as.setType("http");
		as.setPort(8080);
		as.setHost("localhost");
		System.out.println(as.getAuthenticationUrl());
		assertEquals("http://localhost:8080/login?",as.getAuthenticationUrl());
		as.setType(null);
		System.out.println(as.getAuthenticationUrl());
		assertEquals("//localhost:8080/login?",as.getAuthenticationUrl());
		as.setType("http");
		as.setUsername("user");
		as.setPassword("secret");
		System.out.println(as.getAuthorizationUrl());
		assertEquals("http://user:secret@localhost:8080/authorize?",as.getAuthorizationUrl());
		assertEquals("http://user:secret@localhost:8080/logout?",as.getLogoutUrl());
		assertEquals("http://user:secret@localhost:8080/?",as.getLoginUrl());
	}

}
