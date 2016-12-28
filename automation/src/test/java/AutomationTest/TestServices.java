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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static com.jayway.restassured.RestAssured.given;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

@Test(singleThreaded=true,alwaysRun=true)
public class TestServices {

    String gettingStatus;
    Response responseAccessKey;
    String serviceBaseName;
    private String ssnURL;
    private String publicIp;

    final static Logger logger = Logger.getLogger(TestServices.class.getName());
    final static int Forbidden=403;  

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
    
    @AfterClass
    public static void Cleanup() throws InterruptedException {
        sleep(PropertyValue.TEST_AFTER_SLEEP_SECONDS);
    }
    
    
    
    @Test(priority=1)
    public void testJenkinsJob() throws Exception {

        System.out.println("1. Jenkins Job will be started ...");
       
        JenkinsCall jenkins = new JenkinsCall(PropertyValue.get(PropertyValue.JENKINS_USERNANE), PropertyValue.get(PropertyValue.JENKINS_PASSWORD));
        String buildNumber = jenkins.runJenkinsJob(PropertyValue.get(PropertyValue.JENKINS_JOB_URL));
        System.out.println("   Jenkins Job has been completed");
        
        ssnURL = jenkins.getSsnURL().replaceAll(" ", "");
        serviceBaseName = jenkins.getServiceBaseName().replaceAll(" ", "");
        Assert.assertNotNull(ssnURL, "Jenkins URL was not generated");
        Assert.assertNotNull(serviceBaseName, "Service BaseName was not generated");
        System.out.println("Build number is: " + buildNumber);
        System.out.println("JenkinsURL is: " + ssnURL);
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
    
    public String getSnnURL(String path) {
    	return ssnURL + path;
    }
    
    @Test(priority=2)
    public void testLogin() throws Exception {
        
        System.out.println("2. Check login");
        System.out.println("3. Check validation");
        sleep(PropertyValue.TEST_BEFORE_SLEEP_SECONDS);
        
        final String ssnLoginURL = getSnnURL(Path.LOGIN);
        System.out.println("   SSN login URL is " + ssnLoginURL);
        
        LoginDto notIAMUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.NOT_IAM_USERNAME), PropertyValue.get(PropertyValue.NOT_IAM_PASSWORD), "");
        Response responseNotIAMUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notIAMUserRequestBody);
        System.out.println("responseNotIAMUser.statusCode() is " + responseNotIAMUser.statusCode());
        System.out.println("responseNotIAMUser.getBody() is " + responseNotIAMUser.getBody().prettyPrint());
/*        Assert.assertEquals(responseNotIAMUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotIAMUser.getBody().asString(), "Please contact AWS administrator to create corresponding IAM User");
 		*/
        LoginDto notDLABUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.NOT_DLAB_USERNAME), PropertyValue.get(PropertyValue.NOT_DLAB_PASSWORD), "");
        Response responseNotDLABUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notDLABUserRequestBody);
        System.out.println("responseNotDLABUser.statusCode() is " + responseNotDLABUser.statusCode());
        System.out.println("responseNotDLABUser.getBody() is " + responseNotDLABUser.getBody().prettyPrint());
/*        Assert.assertEquals(responseNotDLABUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotDLABUser.getBody().asString(), "Username or password are not valid");
        */
        LoginDto forActivateAccessKey = new LoginDto(PropertyValue.get(PropertyValue.USER_FOR_ACTIVATE_KEY), PropertyValue.get(PropertyValue.PASSWORD_FOR_ACTIVATE_KEY), "");
        Response responseForActivateAccessKey = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, forActivateAccessKey);
        System.out.println("responseForActivateAccessKey.statusCode() is " + responseForActivateAccessKey.statusCode());
        System.out.println("responseForActivateAccessKey.getBody() is " + responseForActivateAccessKey.getBody().prettyPrint());
/*        Assert.assertEquals(responseForActivateAccessKey.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseForActivateAccessKey.getBody().asString(), "Please contact AWS administrator to activate your Access Key");
       */
        LoginDto testUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.USERNANE), PropertyValue.get(PropertyValue.PASSWORD), "");
        Response responseTestUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, testUserRequestBody);
        System.out.println("responseTestUser.statusCode() is " + responseTestUser.statusCode());
/*        Assert.assertEquals(responseTestUser.statusCode(), HttpStatusCode.OK);
 		*/
        System.out.println("4. Check logout");
        final String ssnlogoutURL = getSnnURL(Path.LOGOUT);
        System.out.println("   SSN logout URL is " + ssnlogoutURL);
        
        LoginDto testUserLogin = new LoginDto(PropertyValue.get(PropertyValue.USERNANE), PropertyValue.get(PropertyValue.PASSWORD), "");
        responseTestUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, testUserLogin);
        Response responseLogout = new HttpRequest().webApiPost(ssnlogoutURL, ContentType.ANY);
        System.out.println("responseLogout.statusCode() is " + responseLogout.statusCode());
        Assert.assertEquals(responseLogout.statusCode(), HttpStatusCode.OK);
    }

    @Test(priority=3)
    public void testDLabScenario() throws Exception {

        String noteBookName = "Notebook" + HelperMethods.generateRandomValue();
        String emrName = "EMR" + HelperMethods.generateRandomValue();
        RestAssured.baseURI = ssnURL;
        LoginDto testUserRequestBody = new LoginDto(PropertyValue.get(PropertyValue.USERNANE), PropertyValue.get(PropertyValue.PASSWORD), "");
        
        System.out.println("5. Upload Key will be started ...");
        final String ssnLoginURL = getSnnURL(Path.LOGIN);
        System.out.println("   SSN login URL is " + ssnLoginURL);
        final String ssnUploadKeyURL = getSnnURL(Path.UPLOAD_KEY);
        System.out.println("   SSN login URL is " + ssnUploadKeyURL);

        Response responseTestUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, testUserRequestBody);
        String token = responseTestUser.getBody().asString();
        Response respUploadKey = new HttpRequest().webApiPost(ssnUploadKeyURL, ContentType.FORMDATA, token);
        System.out.println("respUploadKey.statusCode() is " + respUploadKey.statusCode());
        Assert.assertEquals(respUploadKey.statusCode(), HttpStatusCode.OK, "Upload key is not correct");
        
        do {
        	Thread.sleep(1000);
            responseAccessKey = new HttpRequest().webApiGet(ssnUploadKeyURL, token);
            // TODO: Add max timeout
        } while (responseAccessKey.statusCode() == HttpStatusCode.Accepted);
        System.out.println("   Upload Key has been completed");
        System.out.println("responseAccessKey.statusCode() is " + responseAccessKey.statusCode());
        Assert.assertEquals(responseAccessKey.statusCode(), HttpStatusCode.OK, "Upload key is not correct");

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_create_edge_", publicIp);

        Amazon.checkAmazonStatus(serviceBaseName + "-Auto_EPMC-BDCC_Test-edge", AmazonInstanceState.RUNNING);

        System.out.println("7. Notebook will be created ...");
        final String ssnExpEnvURL = getSnnURL(Path.EXP_ENVIRONMENT);
        System.out.println("   SSN exploratory environment URL is " + ssnExpEnvURL);
        final String ssnProUserResURL = getSnnURL(Path.PROVISIONED_RES);
        System.out.println("   SSN provisioned user resources URL is " + ssnProUserResURL);

        CreateNotebookDto createNoteBookRequest = new CreateNotebookDto();
        createNoteBookRequest.setName(noteBookName);
        createNoteBookRequest.setShape("t2.medium");
        createNoteBookRequest.setVersion("jupyter-1.6");
        Response responseCreateNotebook = new HttpRequest().webApiPut(ssnExpEnvURL, ContentType.JSON,
                                                                      createNoteBookRequest, token);
        Assert.assertEquals(responseCreateNotebook.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("creating"));
        System.out.println("   Notebook has been created");
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook was not created");
        System.out.println("Notebook " + noteBookName + " was created");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-nb-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_create_exploratory_NotebookAutoTest", publicIp);

        System.out.println("8. EMR will be deployed ...");
        final String ssnCompResURL = getSnnURL(Path.COMPUTATIONAL_RES);
        System.out.println("   SSN computational resources URL is " + ssnCompResURL);

        
        DeployEMRDto deployEMR = new DeployEMRDto();
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-4.3.0");
        deployEMR.setName(emrName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMR = new HttpRequest().webApiPut(ssnCompResURL, ContentType.JSON,
                                                                    deployEMR, token);
        Assert.assertEquals(responseDeployingEMR.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("computational_resources.status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("creating"));
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR has not deployed");
        System.out.println("    EMR " + emrName + " has been deployed");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-emr-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_create_computational_EMRAutoTest", publicIp);

        System.out.println("9. Notebook will be stopped");
        final String ssnStopNotebookURL = getSnnURL(Path.getStopNotebookUrl(noteBookName));
        System.out.println("   SSN stop notebook URL is " + ssnStopNotebookURL);

        Response responseStopNotebook = new HttpRequest().webApiDelete(ssnStopNotebookURL,
                                                                       ContentType.JSON, token);
        Assert.assertEquals(responseStopNotebook.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("stopping"));
        if (!gettingStatus.contains("stopped"))
            throw new Exception("Notebook was not stopped");
        System.out.println("Notebook " + noteBookName + " was stopped");
        gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
            .getString("computational_resources.status");
        if (!gettingStatus.contains("terminated"))
            throw new Exception("Notebook has not stopped");
        System.out.println("    Notebook " + noteBookName + " has been stopped");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-emr-" + noteBookName, AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_stop_exploratory_NotebookAutoTest", publicIp);

        System.out.println("10. Notebook will be started");
        String myJs = "{\"notebook_instance_name\":\"" + noteBookName + "\"}";
        Response respStartNotebook = new HttpRequest().webApiPost(ssnExpEnvURL, ContentType.JSON,
                                                                  myJs, token);
        Assert.assertEquals(respStartNotebook.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("starting"));
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook was not started");
        System.out.println("Notebook " + noteBookName + " has been started");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-nb-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_start_exploratory_NotebookAutoTest", publicIp);

        System.out.println("11. New EMR will be deployed for termination");
        final String emrNewName = "New" + emrName; 
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-4.3.0");
        deployEMR.setName(emrNewName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRNew = new HttpRequest().webApiPut(ssnCompResURL,
                                                                       ContentType.JSON, deployEMR, token);
        Assert.assertEquals(responseDeployingEMRNew.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("computational_resources.status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("creating"));
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR was not created");
        System.out.println("    New EMR " + emrNewName + " has been deployed");

        System.out.println("    New EMR will be terminated");
        final String ssnTerminateEMRURL = getSnnURL(Path.getTerminateEMRUrl(noteBookName, emrNewName));
        System.out.println("    SSN terminate EMR URL is " + ssnTerminateEMRURL);
        
        Response respTerminateEMR = new HttpRequest().webApiDelete(ssnTerminateEMRURL,
                                                                   ContentType.JSON, token);
        Assert.assertEquals(respTerminateEMR.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("computational_resources.status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("terminating"));
        if (!gettingStatus.contains("terminated"))
            throw new Exception("EMR was not terminated");
        System.out.println("    New EMR " + emrNewName + " has been terminated");

        Amazon.checkAmazonStatus("NewEMRAutoTest", AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_terminate_computational_NewEMRAutoTest", publicIp);

        System.out.println("11. Terminate Notebook");
        final String emrNewName2 = "AnotherNew" + emrName;
        final String ssnTerminateNotebookURL = getSnnURL(Path.getTerminateNotebookUrl(noteBookName));
        System.out.println("    SSN terminate EMR URL is " + ssnTerminateEMRURL);
        
        
        // Deploy EMR
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-4.3.0");
        deployEMR.setName(emrNewName2);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRAnotherNew = new HttpRequest().webApiPut(ssnCompResURL,
                                                                              ContentType.JSON, deployEMR,
                                                                              token);
        Assert.assertEquals(responseDeployingEMRAnotherNew.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("computational_resources.status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("creating"));
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR was not created");
        System.out.println("New emr " + emrNewName2 + " was deployed");

        // terminate notebook
        Response respTerminateNotebook = new HttpRequest().webApiDelete(ssnTerminateNotebookURL, ContentType.JSON, token);
        Assert.assertEquals(respTerminateNotebook.statusCode(), HttpStatusCode.OK);
        do {
            Thread.sleep(1000);
            gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
                .getString("status");
            // TODO: Add max timeout
        } while (gettingStatus.contains("terminating"));
        if (!gettingStatus.contains("terminated"))
            throw new Exception("Notebook was not terminated");
        gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
            .getString("computational_resources.status");
        if (!gettingStatus.contains("terminated"))
            throw new Exception("EMR was not terminated");
        System.out.println("Notebook" + noteBookName + " was terminated");

        Amazon.checkAmazonStatus("Auto_EPMC-BDCC_Test-nb-NotebookAutoTest", AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus("Auto_EPMC-BDCC_Test_terminate_exploratory_NotebookAutoTestt", publicIp);
    }
}
