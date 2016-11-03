package com.epam.dlab.auth.aws;

/**
 * Created by Mikhail_Teplitskiy on 10/28/2016.
 */
import static org.junit.Assert.*;

import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetUserRequest;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.amazonaws.services.identitymanagement.model.ListUsersResult;
import org.junit.*;

import com.epam.dlab.auth.UserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AwsTest {
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
    @Ignore
    public void test1(){
        /*
            AWS account
            575405009735
            AKIAJOA3U4W6PYOHNQ4Q
Secret Access Key:
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
        */
//        AWSCredentials cr = new BasicAWSCredentials("AKIAJOA3U4W6PYOHNQ4Q","1oDSAJZJR6kncn5d+tkyG7o6GrPrMW8ewhPkF4oQ");
        AWSCredentials cr = new BasicAWSCredentials("AKIAJOA3U4W6PYOHNQ4Q","1oDSAJZJR6kncn5d+tkyG7o6GrPrMW8ewhPkF4oQ");
        AmazonIdentityManagement aim = new AmazonIdentityManagementClient(cr);
        //"dmytro_liaskovskyi@epam.com" "mikhail_teplitskiy@epam.com"
        GetUserRequest r = new GetUserRequest().withUserName("dmytro_liaskovskyi@epam.com");
        //r.setUserName("mikhail_teplitskiy@epam.com");
        GetUserResult ur = aim.getUser(r);

        ListUsersResult lur = aim.listUsers();

        System.out.println("UR:"+ur.getUser().getUserName());

        System.out.println("Users:"+lur.getUsers());
    }

}
