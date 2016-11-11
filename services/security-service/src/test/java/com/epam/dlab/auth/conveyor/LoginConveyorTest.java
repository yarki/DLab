package com.epam.dlab.auth.conveyor;

import com.epam.dlab.auth.UserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.*;

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

        lc.addClosure("1",b -> {
            UserInfoBuilder.firstName(b,"user");
            UserInfoBuilder.lastName(b,"test");
        },LoginStep.USER_INFO);
        lc.addClosure("1",b -> {
            UserInfoBuilder.roles(b,new HashSet<String>(){{add("admin");}});
        },LoginStep.GROUP_INFO);
        lc.addClosure("1",b -> {
            UserInfoBuilder.awsUser(b,true);
        },LoginStep.AWS_INFO);
        lc.add("1","127.0.0.1",LoginStep.REMOTE_IP);

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