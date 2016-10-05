package com.epam.dlab.auth.core;

import org.junit.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AuthorizedUsersTest {

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
    public void test() throws InterruptedException {
        AuthorizedUsers.setInactiveTimeout(10);
        AuthorizedUsers users = AuthorizedUsers.getInstance();
        assertNotNull(users);
        users.addUserInfo("a", new UserInfo("test", "a"));
        assertNotNull(users.getUserInfo("a"));
        Thread.sleep(11);
        assertNull(users.getUserInfo("a"));
    }

}
