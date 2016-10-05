package com.epam.dlab.auth.core;

import org.junit.*;

import static org.junit.Assert.assertEquals;

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
        AuthenticationServiceConfig as = new AuthenticationServiceConfig();
        System.out.println(as.getAuthenticateAndRedirectUrl());
        assertEquals("http://localhost/login?", as.getAuthenticateAndRedirectUrl());
        as.setType("http");
        as.setPort(8080);
        as.setHost("localhost");
        System.out.println(as.getAuthenticateAndRedirectUrl());
        assertEquals("http://localhost:8080/login?", as.getAuthenticateAndRedirectUrl());
        as.setType(null);
        System.out.println(as.getAuthenticateAndRedirectUrl());
        assertEquals("//localhost:8080/login?", as.getAuthenticateAndRedirectUrl());
        as.setType("http");
        as.setUsername("user");
        as.setPassword("secret");
        System.out.println(as.getUserInfoUrl());
        assertEquals("http://user:secret@localhost:8080/user_info?", as.getUserInfoUrl());
        assertEquals("http://user:secret@localhost:8080/logout?", as.getLogoutUrl());
        assertEquals("http://user:secret@localhost:8080/?", as.getLoginUrl());
        assertEquals("http://user:secret@localhost:8080/validate?", as.getAccessTokenUrl());
    }

}
