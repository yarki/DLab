package AutomationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.Context;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.Reservation;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.AuthResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import Repository.ContentType;
import Repository.HttpStatusCode;
import Repository.Path;
import ServiceCall.JenkinsCall;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.response.Response;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.Scanner;
import AmazonHelper.Amazon;
import AmazonHelper.AmazonInstanceState;
import DataModel.CreateNotebookDto;
import DataModel.DeployEMRDto;
import DataModel.LoginDto;
import DockerHelper.*;
import Infrastucture.HttpRequest;
import org.apache.http.auth.Credentials;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static com.jayway.restassured.RestAssured.given;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class TestServices {

    String gettingStatus;
    Response responseAccessKey;
    String serviceBaseName;
    private String jenkinsURL;
    private String publicIp;

    final static Logger logger = Logger.getLogger(TestServices.class.getName());
    

    private static void sleep(String propertyName) throws InterruptedException {
    	int timeout = PropertyValue.get(PropertyValue.TEST_BEFORE_SLEEP_SECONDS, 0);
    	if (timeout > 0) {
    		logger.info("Waiting for timeout " + timeout + " seconds.");
    		Thread.sleep(timeout * 1000);
    		logger.info("Timeout is completed.");
    	}
    }

    @BeforeClass
    public static void Setup() throws InterruptedException {
        // loading log4j.xml file
        DOMConfigurator.configure("log4j.xml");
        sleep(PropertyValue.TEST_BEFORE_SLEEP_SECONDS);
    }
    
    @AfterTest
    public static void Cleanup() throws InterruptedException {
        sleep(PropertyValue.TEST_AFTER_SLEEP_SECONDS);
    }
    
    
    
    @Test
    public void testJenkinsJob() throws Exception {

        System.out.println("1. Jenkins Job was started");
       
        JenkinsCall jenkins = new JenkinsCall(PropertyValue.get(PropertyValue.JENKINS_USERNANE), PropertyValue.get(PropertyValue.JENKINS_PASSWORD));
        String buildNumber = jenkins.runJenkinsJob(PropertyValue.get(PropertyValue.JENKINS_JOB_URL));
        jenkinsURL = jenkins.getJenkinsURL().replaceAll(" ", "");
        serviceBaseName = jenkins.getServiceBaseName().replaceAll(" ", "");
        Assert.assertNotNull(jenkinsURL, "Jenkins URL was not generated");
        Assert.assertNotNull(serviceBaseName, "Service BaseName was not generated");
        System.out.println("Build number is: " + buildNumber);
        System.out.println("JenkinsURL is: " + jenkinsURL);
        System.out.println("ServiceBaseName is: " + serviceBaseName);

        System.out.println("Check status of SSN node on Amazon:");
        DescribeInstancesResult describeInstanceResult = Amazon.getInstanceResult(serviceBaseName + "-ssn");
        InstanceState instanceState = describeInstanceResult.getReservations().get(0).getInstances().get(0)
            .getState();
        publicIp = describeInstanceResult.getReservations().get(0).getInstances().get(0)
            .getPublicIpAddress();
        System.out.println("Public Ip is: " + publicIp);
        Assert.assertEquals(instanceState.getName(), AmazonInstanceState.RUNNING,
                            "Amazon instance state is not running");
        System.out.println("Amazon instance state is running");
    }
    
    @Test(priority=1)
    public void testLogin() throws Exception {
        
        System.out.println("2. Check login");
        System.out.println("3. Check validation");
        Thread.sleep(timeout * 1000);
        LoginDto notIAMUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.NOT_IAM_USERNAME), PropertyValue.get(PropertyValue.NOT_IAM_PASSWORD), "");
        Response responseNotIAMUser = new HttpRequest().webApiPost(Path.LOGIN, ContentType.JSON, notIAMUserRequestBody);
        Assert.assertEquals(responseNotIAMUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotIAMUser.getBody().asString(), "Please contact AWS administrator to create corresponding IAM User");
        
        LoginDto notDLABUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.NOT_DLAB_USERNAME), PropertyValue.get(PropertyValue.NOT_DLAB_PASSWORD), "");
        Response responseNotDLABUser = new HttpRequest().webApiPost(Path.LOGIN, ContentType.JSON, notDLABUserRequestBody);
        Assert.assertEquals(responseNotDLABUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotDLABUser.getBody().asString(), "Username or password are not valid");
        
        LoginDto forActivateAccessKey = new LoginDto(PropertyValue.get(PropertyValue.USER_FOR_ACTIVATE_KEY), PropertyValue.get(PropertyValue.PASSWORD_FOR_ACTIVATE_KEY), "");
        Response responseForActivateAccessKey = new HttpRequest().webApiPost(Path.LOGIN, ContentType.JSON, forActivateAccessKey);
        Assert.assertEquals(responseForActivateAccessKey.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseForActivateAccessKey.getBody().asString(), "Please contact AWS administrator to activate your Access Key");
       
        LoginDto testUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.USERNANE), PropertyValue.get(PropertyValue.PASSWORD), "");
        Response responseTestUser = new HttpRequest().webApiPost(Path.LOGIN, ContentType.JSON, testUserRequestBody);
        Assert.assertEquals(responseTestUser.statusCode(), HttpStatusCode.OK);
               
        System.out.println("4. Check logout");
        LoginDto testUserLogin = new LoginDto(PropertyValue.get(PropertyValue.USERNANE), PropertyValue.get(PropertyValue.PASSWORD), "");
        responseTestUser = new HttpRequest().webApiPost(Path.LOGIN, ContentType.JSON, testUserLogin);
        Response responseLogout = new HttpRequest().webApiPost(Path.LOGOUT, ContentType.ANY);
        Assert.assertEquals(responseLogout.statusCode(), HttpStatusCode.OK);
    }

    @Test(priority=2)
    public void testDLabScenario() throws Exception {

        String noteBookName = "Notebook" + HelperMethods.generateRandomValue();
        String emrName = "EMR" + HelperMethods.generateRandomValue();
        RestAssured.baseURI = jenkinsURL;
        LoginDto testUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.USERNANE), PropertyValue.get(PropertyValue.PASSWORD), "");
        
        System.out.println("5. Upload Key");
        Response responseTestUser = new HttpRequest().webApiPost(Path.LOGIN, ContentType.JSON, testUserRequestBody);
        String token = responseTestUser.getBody().asString();
        Response respUploadKey = new HttpRequest().webApiPost(Path.UPLOAD_KEY, ContentType.FORMDATA, token);
        Assert.assertEquals(respUploadKey.statusCode(), HttpStatusCode.OK, "Upload key is not correct");
        do {
            responseAccessKey = new HttpRequest().webApiGet(Path.UPLOAD_KEY, token);
        } while (responseAccessKey.statusCode() == HttpStatusCode.Accepted);
        Assert.assertEquals(responseAccessKey.statusCode(), HttpStatusCode.OK, "Upload key is not correct");

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_create_edge_", publicIp);

        Amazon.checkAmazonStatus(serviceBaseName + "-Auto_EPMC-BDCC_Test-edge", AmazonInstanceState.RUNNING);

        System.out.println("7. Create Notebook");
        CreateNotebookDto createNoteBookRequest = new CreateNotebookDto();
        createNoteBookRequest.setName(noteBookName);
        createNoteBookRequest.setShape("t2.medium");
        createNoteBookRequest.setVersion("jupyter-1.6");
        Response responseCreateNotebook = new HttpRequest().webApiPut(Path.EXP_ENVIRONMENT, ContentType.JSON,
                                                                      createNoteBookRequest, token);
        Assert.assertEquals(responseCreateNotebook.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("status");
        } while (gettingStatus.contains("creating"));
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook was not created");
        System.out.println("Notebook " + noteBookName + " was created");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-nb-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_create_exploratory_NotebookAutoTest", publicIp);

        System.out.println("8. Deploy EMR");
        DeployEMRDto deployEMR = new DeployEMRDto();
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-4.3.0");
        deployEMR.setName(emrName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMR = new HttpRequest().webApiPut(Path.COMPUTATIONAL_RES, ContentType.JSON,
                                                                    deployEMR, token);
        Assert.assertEquals(responseDeployingEMR.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("computational_resources.status");
        } while (gettingStatus.contains("creating"));
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR was not created");
        System.out.println("Emr " + emrName + " was deployed");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-emr-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_create_computational_EMRAutoTest", publicIp);

        System.out.println("9. Stop Notebook");
        Response responseStopNotebook = new HttpRequest().webApiDelete(Path.getStopNotebookUrl(noteBookName),
                                                                       ContentType.JSON, token);
        Assert.assertEquals(responseStopNotebook.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("status");
        } while (gettingStatus.contains("stopping"));
        if (!gettingStatus.contains("stopped"))
            throw new Exception("Notebook was not stopped");
        System.out.println("Notebook " + noteBookName + " was stopped");
        gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
            .getString("computational_resources.status");
        if (!gettingStatus.contains("terminated"))
            throw new Exception("EMR was not terminated");
        System.out.println("Emr " + emrName + " was terminated");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-emr-" + noteBookName, AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_stop_exploratory_NotebookAutoTest", publicIp);

        System.out.println("10. Start Notebook");
        String myJs = "{\"notebook_instance_name\":\"" + noteBookName + "\"}";
        Response respStartNotebook = new HttpRequest().webApiPost(Path.EXP_ENVIRONMENT, ContentType.JSON,
                                                                  myJs, token);
        Assert.assertEquals(respStartNotebook.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("status");
        } while (gettingStatus.contains("starting"));
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook was not started");
        System.out.println("Notebook " + noteBookName + " was started");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-nb-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_start_exploratory_NotebookAutoTest", publicIp);

        System.out.println("11. Terminate EMR");
        // Deploy EMR
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-4.3.0");
        deployEMR.setName("New" + emrName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRNew = new HttpRequest().webApiPut(Path.COMPUTATIONAL_RES,
                                                                       ContentType.JSON, deployEMR, token);
        Assert.assertEquals(responseDeployingEMRNew.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("computational_resources.status");
        } while (gettingStatus.contains("creating"));
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR was not created");
        System.out.println("New emr " + "New" + emrName + " was deployed");
        // terminate EMR
        Response respTerminateEMR = new HttpRequest().webApiDelete(Path.getTerminateEMRUrl(noteBookName,
                                                                                           "New" + emrName),
                                                                   ContentType.JSON, token);
        Assert.assertEquals(respTerminateEMR.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("computational_resources.status");
        } while (gettingStatus.contains("terminating"));
        if (!gettingStatus.contains("terminated"))
            throw new Exception("EMR was not terminated");
        System.out.println("Emr " + "New" + emrName + " was terminated");

        Amazon.checkAmazonStatus("NewEMRAutoTest", AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_terminate_computational_NewEMRAutoTest", publicIp);

        System.out.println("11. Terminate Notebook");
        // Deploy EMR
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-4.3.0");
        deployEMR.setName("AnotherNew" + emrName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRAnotherNew = new HttpRequest().webApiPut(Path.COMPUTATIONAL_RES,
                                                                              ContentType.JSON, deployEMR,
                                                                              token);
        Assert.assertEquals(responseDeployingEMRAnotherNew.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("computational_resources.status");
        } while (gettingStatus.contains("creating"));
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR was not created");
        System.out.println("New emr " + "AnotherNew" + emrName + " was deployed");

        // terminate notebook
        Response respTerminateNotebook = new HttpRequest().webApiDelete(Path
            .getTerminateNotebookUrl(noteBookName), ContentType.JSON, token);
        Assert.assertEquals(respTerminateNotebook.statusCode(), HttpStatusCode.OK);
        do {
            gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
                .getString("status");
        } while (gettingStatus.contains("terminating"));
        if (!gettingStatus.contains("terminated"))
            throw new Exception("Notebook was not terminated");
        gettingStatus = new HttpRequest().webApiGet(Path.PROVISIONED_RES, token).getBody().jsonPath()
            .getString("computational_resources.status");
        if (!gettingStatus.contains("terminated"))
            throw new Exception("EMR was not terminated");
        System.out.println("Notebook" + noteBookName + " was terminated");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-nb-NotebookAutoTest", AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_terminate_exploratory_NotebookAutoTestt", publicIp);
    }
}
