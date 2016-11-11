package com.epam.dlab.auth.conveyor;

import com.epam.dlab.auth.UserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public class LoginConveyorTest {

    LoginConveyor lc = new LoginConveyor();

    @Before
    public void createCOnveyor(){
    }

    @After
    public void stopConveyor() {
    }

    @Test
    public void setUserInfoDao() throws Exception {

    }

    @Test
    public void startUserInfoBuild() throws Exception {
        CompletableFuture<UserInfo> uf = lc.startUserInfoBuild("1","test");
        UserInfo uiSource = new UserInfo("a","b");
        uiSource.setFirstName("test");
        uiSource.setLastName("user");
        uiSource.addRole("admin");

        lc.add("1","127.0.0.1",LoginStep.REMOTE_IP);
        lc.add("1",uiSource,LoginStep.LDAP_USER_INFO);
        lc.add("1",true,LoginStep.AWS_USER);

        UserInfo ui = uf.get(5, TimeUnit.SECONDS);
        System.out.println("Future now: "+ui);
    }

    @Test
    public void add() throws Exception {

    }

    @Test
    public void cancel() throws Exception {

    }

}