package com.epam.dlab.auth.conveyor;

import com.epam.dlab.auth.UserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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

    @Test(expected = CancellationException.class)
    public void cacheTest() throws ExecutionException, InterruptedException {
        LoginCache lc = LoginCache.getInstance();

        //Just for this test
        lc.setDefaultBuilderTimeout(1,TimeUnit.SECONDS);
        lc.setExpirationPostponeTime(1,TimeUnit.SECONDS);

        UserInfo userInfo = new UserInfo("test","user");
        userInfo.setFirstName("Mike");
        userInfo.setLastName("T");
        userInfo.addRole("tr");
        userInfo.setAwsUser(true);

        CompletableFuture<Boolean> f = lc.add("1",userInfo,LoginStep.USER_INFO);
        CompletableFuture<UserInfo> uif = lc.getFuture("1");
        f.get();
        //this will take at least 2 seconds
        for(int i = 0; i < 10; i++) {
            UserInfo ui = lc.getUserInfo("1");
            System.out.println(i+": "+ui);
            Thread.sleep(200);
        }
        //and finally will exit with timeout
        uif.get();
    }

    @Test
    public void add() throws Exception {

    }

    @Test
    public void cancel() throws Exception {

    }

}