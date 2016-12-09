package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.utils.caching.ImmutableReference;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.epam.dlab.auth.UserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.*;

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
        lc.add("1","OK",LoginStep.LDAP_LOGIN);
        lc.add("1",uiSource,LoginStep.LDAP_USER_INFO);
        lc.add("1",true,LoginStep.AWS_USER);
        lc.add("1",new ArrayList<AccessKeyMetadata>(){{add(new AccessKeyMetadata().withAccessKeyId("a").withStatus("Active"));}} ,LoginStep.AWS_KEYS);

        UserInfo ui = uf.get(5, TimeUnit.SECONDS);
        System.out.println("Future now: "+ui);
    }

    @Test(expected = CancellationException.class)
    public void cacheTest() throws ExecutionException, InterruptedException, TimeoutException {
        LoginCache cache = LoginCache.getInstance();
System.out.println("---cacheTest");
        //Just for this test
        cache.setDefaultBuilderTimeout(1,TimeUnit.SECONDS);
        cache.setExpirationPostponeTime(1,TimeUnit.SECONDS);

        UserInfo userInfo = new UserInfo("test","user");
        userInfo.setFirstName("Mike");
        userInfo.setLastName("T");
        userInfo.addRole("tr");
        userInfo.setAwsUser(true);
        userInfo.addKey("a","Active");

        CompletableFuture<Boolean> f = cache.createBuild("2",new ImmutableReference<UserInfo>(userInfo));
        CompletableFuture<UserInfo> uif = cache.getFuture("2");
        f.get();
        //this will take at least 2 seconds
        for(int i = 0; i < 10; i++) {
            UserInfo ui = cache.getUserInfo("1");
            System.out.println(i+": "+ui);
            Thread.sleep(200);
        }
        //and finally will exit with timeout
        uif.get(5,TimeUnit.SECONDS);
    }

    @Test
    public void add() throws Exception {

    }

    @Test
    public void cancel() throws Exception {

    }

}