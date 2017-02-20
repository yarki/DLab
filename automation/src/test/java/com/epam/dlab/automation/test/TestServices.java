package com.epam.dlab.automation.test;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Tag;
import com.epam.dlab.automation.aws.AmazonHelper;
import com.epam.dlab.automation.aws.AmazonInstanceState;
import com.epam.dlab.automation.aws.NodeReader;
import com.epam.dlab.automation.docker.AckStatus;
import com.epam.dlab.automation.docker.Docker;
import com.epam.dlab.automation.docker.SSHConnect;
import com.epam.dlab.automation.helper.ConfigPropertyValue;
import com.epam.dlab.automation.helper.PropertiesResolver;
import com.epam.dlab.automation.helper.TestNamingHelper;
import com.epam.dlab.automation.http.HttpRequest;
import com.epam.dlab.automation.http.HttpStatusCode;
import com.epam.dlab.automation.jenkins.JenkinsService;
import com.epam.dlab.automation.model.CreateNotebookDto;
import com.epam.dlab.automation.model.DeployEMRDto;
import com.epam.dlab.automation.model.LoginDto;
import com.epam.dlab.automation.repository.ApiPath;
import com.epam.dlab.automation.repository.ContentType;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Test(singleThreaded=true)
public class TestServices {

    private final static Logger LOGGER = LogManager.getLogger(TestServices.class);
    private final static long SSN_REQUEST_TIMEOUT = 10000; 

	private String serviceBaseName;
    private String ssnURL;
    private String publicIp;

    @BeforeClass
    public void Setup() throws InterruptedException {

        // Load properties
        ConfigPropertyValue.getJenkinsJobURL();
    }
    
    @AfterClass
    public void Cleanup() throws InterruptedException {
    }

    @Test(priority = 0)
    public void runJenkins() throws Exception {
        LOGGER.info("Test Started");
    	testJenkinsJob();
        LOGGER.info("Test Finished");
    }


    @Test(priority = 1)
    public void runLogin() throws Exception {
        LOGGER.info("Test Started");
        testLogin();
        LOGGER.info("Test Finished");
    }


    @Test(priority = 2)
    public void runDLabScenario() throws Exception {
        LOGGER.info("Test Started");
        testDLabScenario();
        LOGGER.info("Test Finished");
    }

    private void testJenkinsJob() throws Exception {

        /* LOGGER.info("1. Jenkins Job will be started ...");
       
        JenkinsService jenkins = new JenkinsService(ConfigPropertyValue.getJenkinsUsername(), ConfigPropertyValue.getJenkinsPassword());
        String buildNumber = jenkins.runJenkinsJob(ConfigPropertyValue.getJenkinsJobURL());
        LOGGER.info("   Jenkins Job has been completed"); */

        LOGGER.info("1. Looking for last Jenkins Job ...");
        JenkinsService jenkins = new JenkinsService();
        String buildNumber = jenkins.getJenkinsJob();
        LOGGER.info("   Jenkins Job found:");
        LOGGER.info("Build number is: {}", buildNumber);
        
        ssnURL = jenkins.getSsnURL().replaceAll(" ", "");
        serviceBaseName = jenkins.getServiceBaseName().replaceAll(" ", "");
        Assert.assertNotNull(ssnURL, "Jenkins URL was not generated");
        Assert.assertNotNull(serviceBaseName, "Service BaseName was not generated");
        LOGGER.info("JenkinsURL is: " + ssnURL);
        LOGGER.info("ServiceBaseName is: " + serviceBaseName);

        LOGGER.info("Check status of SSN node on AmazonHelper:");
        Instance ssnInstance = AmazonHelper.getInstance(serviceBaseName + "-ssn");
        InstanceState instanceState = ssnInstance.getState();
        publicIp = ssnInstance.getPublicIpAddress();
        LOGGER.info("Public Ip is: {}", publicIp);
        Assert.assertEquals(instanceState.getName(), AmazonInstanceState.RUNNING.value(),
                            "AmazonHelper instance state is not running");
        LOGGER.info("AmazonHelper instance state is running");
    }
    
    private void testLogin() throws Exception {
    	
    	//ssnURL = "http://ec2-35-162-89-115.us-west-2.compute.amazonaws.com";
        
        LOGGER.info("2. Waiting for SSN service ...");
        Assert.assertEquals(waitForSSNService(ConfigPropertyValue.getTimeoutNotebookCreate()), true, "SSN service was not started");
        LOGGER.info("   SSN service is available");
        
        
        LOGGER.info("3. Check login");
        final String ssnLoginURL = getSnnURL(ApiPath.LOGIN);
        LOGGER.info("   SSN login URL is {}", ssnLoginURL);
        
        LoginDto notIAMUserRequestBody = new LoginDto(ConfigPropertyValue.getNotIAMUsername(), ConfigPropertyValue.getNotIAMPassword(), "");
        Response responseNotIAMUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notIAMUserRequestBody);
        Assert.assertEquals(responseNotIAMUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotIAMUser.getBody().asString(), "Please contact AWS administrator to create corresponding IAM User");
 		
        LoginDto notDLABUserRequestBody = new LoginDto(ConfigPropertyValue.getNotDLabUsername(), ConfigPropertyValue.getNotDLabPassword(), "");
        Response responseNotDLABUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notDLABUserRequestBody);
        Assert.assertEquals(responseNotDLABUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotDLABUser.getBody().asString(), "Username or password are not valid");
        
        LoginDto forActivateAccessKey = new LoginDto(ConfigPropertyValue.getUsername(), ".", "");
        Response responseForActivateAccessKey = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, forActivateAccessKey);
        Assert.assertEquals(responseForActivateAccessKey.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseForActivateAccessKey.getBody().asString(), "Username or password are not valid");
        
        LoginDto testUserLogin = new LoginDto(ConfigPropertyValue.getUsername(), ConfigPropertyValue.getPassword(), "");
        LOGGER.info("Logging in with credentials {}:{}", ConfigPropertyValue.getUsername(), ConfigPropertyValue.getPassword());
        Response responseTestUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, testUserLogin);
        Assert.assertEquals(responseTestUser.statusCode(), HttpStatusCode.OK);
 		
        
        LOGGER.info("4. Check logout");
        final String ssnlogoutURL = getSnnURL(ApiPath.LOGOUT);
        LOGGER.info("   SSN logout URL is {}", ssnlogoutURL);
        
        Response responseLogout = new HttpRequest().webApiPost(ssnlogoutURL, ContentType.ANY);
        LOGGER.info("responseLogout.statusCode() is {}", responseLogout.statusCode());
        Assert.assertEquals(responseLogout.statusCode(), HttpStatusCode.Unauthorized/*Replace to HttpStatusCode.OK when EPMCBDCCSS-938 will be fixed and merged*/);
    }

    private void testDLabScenario() throws Exception {

    	//ssnURL = "http://ec2-35-164-76-52.us-west-2.compute.amazonaws.com";
        //serviceBaseName = "autotest_jan11";
        //publicIp = "35.164.76.52";

        String gettingStatus;
        String noteBookName = "Notebook" + TestNamingHelper.generateRandomValue();
        String emrName = "eimr" + TestNamingHelper.generateRandomValue();
        
        final String nodePrefix = ConfigPropertyValue.getUsernameSimple();
        final String amazonNodePrefix = serviceBaseName + "-" + nodePrefix;

        RestAssured.baseURI = ssnURL;
        LoginDto testUserRequestBody = new LoginDto(ConfigPropertyValue.getUsername(), ConfigPropertyValue.getPassword(), "");

        
        LOGGER.info("5. Logging user in...");

        final String ssnLoginURL = getSnnURL(ApiPath.LOGIN);
        LOGGER.info("   SSN login URL is {}", ssnLoginURL);

        final String ssnUploadKeyURL = getSnnURL(ApiPath.UPLOAD_KEY);
        LOGGER.info("   SSN upload key URL is {}", ssnUploadKeyURL);

        Response responseTestUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, testUserRequestBody);
        Assert.assertEquals(HttpStatusCode.OK, responseTestUser.getStatusCode(), "Failed to login");
        String token = responseTestUser.getBody().asString();
        LOGGER.info("   Logged in. Obtained token: {}", token);

        
        LOGGER.info("5.a Checking for user Key...");
        Response respCheckKey = new HttpRequest().webApiGet(ssnUploadKeyURL, token);

        if(respCheckKey.getStatusCode() == HttpStatusCode.NotFound) {
            LOGGER.info("5.b Upload Key will be started ...");

            Response respUploadKey = new HttpRequest().webApiPost(ssnUploadKeyURL, ContentType.FORMDATA, token);
            LOGGER.info("   respUploadKey.getBody() is {}", respUploadKey.getBody().asString());

            Assert.assertEquals(respUploadKey.statusCode(), HttpStatusCode.OK, "The key uploading was not successful");
            int responseCodeAccessKey = waitWhileStatus(ssnUploadKeyURL, token, HttpStatusCode.Accepted, ConfigPropertyValue.getTimeoutUploadKey());
            LOGGER.info("   Upload Key has been completed");
            LOGGER.info("responseAccessKey.statusCode() is {}", responseCodeAccessKey);
            Assert.assertEquals(responseCodeAccessKey, HttpStatusCode.OK, "The key uploading was not successful");
        } else if (respCheckKey.getStatusCode() == HttpStatusCode.OK){
            LOGGER.info("   Key has been uploaded already");
        } else {
            Assert.assertEquals(200, respCheckKey.getStatusCode(), "Failed to check User Key.");
        }

        Docker.checkDockerStatus(nodePrefix + "_create_edge_", publicIp);
        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-edge", AmazonInstanceState.RUNNING.value());

        
        LOGGER.info("6. Notebook will be created ...");
        final String ssnExpEnvURL = getSnnURL(ApiPath.EXP_ENVIRONMENT);
        LOGGER.info("   SSN exploratory environment URL is {}", ssnExpEnvURL);
        final String ssnProUserResURL = getSnnURL(ApiPath.PROVISIONED_RES);
        LOGGER.info("   SSN provisioned user resources URL is {}", ssnProUserResURL);

        //todo: read from file
        //TODO: hardcoded names, shapes, versions, etc.
        CreateNotebookDto createNoteBookRequest = //new CreateNotebookDto();
                NodeReader.readNode(
                Paths.get( PropertiesResolver.getClusterConfFileLocation(), "notebook1.json").toString(),
                CreateNotebookDto.class);
//        createNoteBookRequest.setImage("docker.epmc-bdcc.projects.epam.com/dlab-aws-jupyter");
//        createNoteBookRequest.setImage("docker.dlab-jupyter");
        //docker.dlab-jupyter
//        docker.dlab-
//        createNoteBookRequest.setTemplateName("Jupyter 1.5");
        createNoteBookRequest.setName(noteBookName);
//        createNoteBookRequest.setShape("r3.xlarge");
//        createNoteBookRequest.setVersion("jupyter-1.6");
        Response responseCreateNotebook = new HttpRequest().webApiPut(ssnExpEnvURL, ContentType.JSON,
                                                                      createNoteBookRequest, token);
        LOGGER.info("   responseCreateNotebook.getBody() is {}", responseCreateNotebook.getBody().asString());
        Assert.assertEquals(responseCreateNotebook.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "creating", ConfigPropertyValue.getTimeoutNotebookCreate());
        if (!gettingStatus.contains("running")) {
            LOGGER.error("Notebook {} is in state {}", noteBookName, gettingStatus);
            throw new Exception("Notebook " + noteBookName + " has not been created");
        }
        LOGGER.info("   Notebook {} has been created", noteBookName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-nb-" + noteBookName, AmazonInstanceState.RUNNING.value());
        Docker.checkDockerStatus(nodePrefix + "_create_exploratory_NotebookAutoTest", publicIp);

        //get notebook IP
        String notebookIp = AmazonHelper.getInstance(amazonNodePrefix + "-nb-" + noteBookName)
        		.getPrivateIpAddress();
        
        
        LOGGER.info("7. EMR will be deployed ...");
        final String ssnCompResURL = getSnnURL(ApiPath.COMPUTATIONAL_RES);
        LOGGER.info("   SSN computational resources URL is {}", ssnCompResURL);

        DeployEMRDto deployEMR =
                NodeReader.readNode(
                Paths.get(PropertiesResolver.getClusterConfFileLocation(), "EMR.json").toString(),
                DeployEMRDto.class);
//                new DeployEMRDto();
//        final String emrVersion="emr-5.2.0";
//        deployEMR.setEmr_instance_count("2"); // TODO: Set to 3
//        deployEMR.setEmr_master_instance_type("m4.large");
//        deployEMR.setEmr_slave_instance_type("m4.large");
//        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMR = new HttpRequest().webApiPut(ssnCompResURL, ContentType.JSON,
                                                                    deployEMR, token);
        LOGGER.info("   responseDeployingEMR.getBody() is {}", responseDeployingEMR.getBody().asString());
        Assert.assertEquals(responseDeployingEMR.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrName, "creating", ConfigPropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("configuring"))
            throw new Exception("EMR " + emrName + " has not been deployed");
        LOGGER.info("   EMR {} has been deployed", emrName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName, AmazonInstanceState.RUNNING.value());
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);
        
        LOGGER.info("   Waiting until EMR has been configured ...");

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrName, "configuring", ConfigPropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR " + emrName + " has not been configured");
        LOGGER.info("   EMR {} has been configured", emrName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName, AmazonInstanceState.RUNNING.value());
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);

        LOGGER.info("   Check bucket {}", getBucketName());
        AmazonHelper.printBucketGrants(getBucketName());
        
        //run python script
        testPython(publicIp, notebookIp, emrName, getEmrClusterName(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName));

        
        LOGGER.info("8. Notebook will be stopped ...");
        final String ssnStopNotebookURL = getSnnURL(ApiPath.getStopNotebookUrl(noteBookName));
        LOGGER.info("   SSN stop notebook URL is {}", ssnStopNotebookURL);

        Response responseStopNotebook = new HttpRequest().webApiDelete(ssnStopNotebookURL,
                                                                       ContentType.JSON, token);
        LOGGER.info("   responseStopNotebook.getBody() is {}", responseStopNotebook.getBody().asString());
        Assert.assertEquals(responseStopNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "stopping", ConfigPropertyValue.getTimeoutNotebookShutdown());
        if (!gettingStatus.contains("stopped"))
            throw new Exception("Notebook " + noteBookName + " has not been stopped");
        LOGGER.info("   Notebook {} has been stopped", noteBookName);
        gettingStatus = getEmrStatus(
        					new HttpRequest()
        						.webApiGet(ssnProUserResURL, token)
        						.getBody()
        						.jsonPath(),
        					noteBookName, emrName);

        if (!gettingStatus.contains("terminated"))
            throw new Exception("Computational resources has not been terminated for Notebook " + noteBookName);
        LOGGER.info("   Computational resources has been terminated for Notebook {}", noteBookName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName, AmazonInstanceState.TERMINATED.value());
        Docker.checkDockerStatus(nodePrefix + "_stop_exploratory_NotebookAutoTest", publicIp);

        
        LOGGER.info("9. Notebook will be started ...");
        String myJs = "{\"notebook_instance_name\":\"" + noteBookName + "\"}";
        Response respStartNotebook = new HttpRequest().webApiPost(ssnExpEnvURL, ContentType.JSON,
                                                                  myJs, token);
        LOGGER.info("    respStartNotebook.getBody() is {}", respStartNotebook.getBody().asString());
        Assert.assertEquals(respStartNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "starting", ConfigPropertyValue.getTimeoutNotebookStartup());
        if (!gettingStatus.contains(AmazonInstanceState.RUNNING.value()))
            throw new Exception("Notebook " + noteBookName + " has not been started");
        LOGGER.info("    Notebook {} has been started", noteBookName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-nb-" + noteBookName, AmazonInstanceState.RUNNING.value());
        Docker.checkDockerStatus(nodePrefix + "_start_exploratory_NotebookAutoTest", publicIp);

        
        LOGGER.info("10. New EMR will be deployed for termination ...");
        //todo: read from file
        final String emrNewName = "New" + emrName; 
//        deployEMR.setEmr_instance_count("2");
//        deployEMR.setEmr_master_instance_type("m4.large");
//        deployEMR.setEmr_slave_instance_type("m4.large");
//        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrNewName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRNew = new HttpRequest().webApiPut(ssnCompResURL,
                                                                       ContentType.JSON, deployEMR, token);
        LOGGER.info("    responseDeployingEMRNew.getBody() is {}", responseDeployingEMRNew.getBody().asString());
        Assert.assertEquals(responseDeployingEMRNew.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName, "creating", ConfigPropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("configuring"))
            throw new Exception("New EMR " + emrNewName + " has not been deployed");
        LOGGER.info("    New EMR {} has been deployed", emrNewName);
        
        LOGGER.info("   Waiting until EMR has been configured ...");
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName, "configuring", ConfigPropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains(AmazonInstanceState.RUNNING.value()))
            throw new Exception("EMR " + emrNewName + " has not been configured");
        LOGGER.info("   EMR {} has been configured", emrNewName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName, AmazonInstanceState.RUNNING.value());
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);

        LOGGER.info("    New EMR will be terminated ...");
        final String ssnTerminateEMRURL = getSnnURL(ApiPath.getTerminateEMRUrl(noteBookName, emrNewName));
        LOGGER.info("    SSN terminate EMR URL is {}", ssnTerminateEMRURL);
        
        Response respTerminateEMR = new HttpRequest().webApiDelete(ssnTerminateEMRURL,
                                                                   ContentType.JSON, token);
        LOGGER.info("    respTerminateEMR.getBody() is {}", respTerminateEMR.getBody().asString());
        Assert.assertEquals(respTerminateEMR.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName, "terminating", ConfigPropertyValue.getTimeoutEMRTerminate());
        if (!gettingStatus.contains("terminated"))
            throw new Exception("New EMR " + emrNewName + " has not been terminated");
        LOGGER.info("    New EMR {} has been terminated", emrNewName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName, AmazonInstanceState.TERMINATED.value());
        Docker.checkDockerStatus(nodePrefix + "_terminate_computational_NewEMRAutoTest", publicIp);

        
        LOGGER.info("11. New EMR will be deployed for notebook termination ...");
        final String emrNewName2 = "AnotherNew" + emrName;
        
        LOGGER.info("    SSN terminate EMR URL is {}", ssnTerminateEMRURL);
		LOGGER.info("    New EMR will be deployed ...");
//        deployEMR.setEmr_instance_count("2");
//        deployEMR.setEmr_master_instance_type("m4.large");
//        deployEMR.setEmr_slave_instance_type("m4.large");
//        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrNewName2);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRAnotherNew = new HttpRequest().webApiPut(ssnCompResURL,
                                                                              ContentType.JSON, deployEMR,
                                                                              token);
        LOGGER.info("    responseDeployingEMRAnotherNew.getBody() is {}", responseDeployingEMRAnotherNew.getBody().asString());
        Assert.assertEquals(responseDeployingEMRAnotherNew.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName2, "creating", ConfigPropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("configuring"))
            throw new Exception("New emr " + emrNewName2 + " has not been deployed");
        LOGGER.info("    New emr {} has been deployed", emrNewName2);
        
        LOGGER.info("   Waiting until EMR has been configured ...");
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName2, "configuring", ConfigPropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains(AmazonInstanceState.RUNNING.value()))
            throw new Exception("EMR " + emrNewName2 + " has not been configured");
        LOGGER.info("   EMR  {} has been configured", emrNewName2);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName2, AmazonInstanceState.RUNNING.value());
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);

        
        LOGGER.info("12. Notebook will be terminated ...");
        final String ssnTerminateNotebookURL = getSnnURL(ApiPath.getTerminateNotebookUrl(noteBookName));
        Response respTerminateNotebook = new HttpRequest().webApiDelete(ssnTerminateNotebookURL, ContentType.JSON, token);
        LOGGER.info("    respTerminateNotebook.getBody() is {}", respTerminateNotebook.getBody().asString());
        Assert.assertEquals(respTerminateNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "terminating", ConfigPropertyValue.getTimeoutEMRTerminate());
        if (!gettingStatus.contains("terminated"))
            throw new Exception("Notebook" + noteBookName + " has not been terminated");

        gettingStatus = getEmrStatus(
				new HttpRequest()
					.webApiGet(ssnProUserResURL, token)
					.getBody()
					.jsonPath(),
				noteBookName, emrNewName2);
        if (!gettingStatus.contains("terminated"))
            throw new Exception("EMR has not been terminated for Notebook " + noteBookName);
        LOGGER.info("    EMR has been terminated for Notebook {}", noteBookName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-nb-NotebookAutoTest", AmazonInstanceState.TERMINATED.value());
        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName2, AmazonInstanceState.TERMINATED.value());

        Docker.checkDockerStatus(nodePrefix + "_terminate_exploratory_NotebookAutoTestt", publicIp);
    }
    
    private String getBucketName() {
    	return String.format("%s-%s-bucket", serviceBaseName, ConfigPropertyValue.getUsernameSimple()).replace('_', '-').toLowerCase();
    }
    
    private String getEmrClusterName(String emrName) throws Exception {
        Instance instance = AmazonHelper.getInstance(emrName);
        for (Tag tag : instance.getTags()) {
			if (tag.getKey().equals("Name")) {
		        return tag.getValue();
			}
		}
        throw new Exception("Could not detect cluster name for EMR " + emrName);
    }

//    private void copyFileToSSN(String filename, String ip) throws IOException, InterruptedException {
//        String sourceDir = PropertiesResolver.getPythonFilesLocation();
//
//        LOGGER.info("Copying {}...", filename);
//        String command = String.format(ScpCommands.copyToSSNCommand,
//        		ConfigPropertyValue.getAccessKeyPrivFileName(),
//                Paths.get(sourceDir, filename).toString(),
//                ip);
//        AckStatus status = HelperMethods.executeCommand(command);
//        LOGGER.info("Copied {}: {}", filename, status.toString());
//        Assert.assertTrue(status.isOk());
//    }


//    private void copyFileToSSN2(Session session, String filename, String ssnIp) throws IOException, InterruptedException, JSchException {
//        String sourceDir = PropertiesResolver.getPythonFilesLocation();
//
//        LOGGER.info("Copying {}...", filename);
//        Assert.assertTrue(new File(Paths.get(sourceDir, filename).toString()).exists());
//        String command = String.format(ScpCommands.copyToSSNCommand,
//                ConfigPropertyValue.getAccessKeyPrivFileName(),
//                Paths.get(sourceDir, filename).toString(),
//                ssnIp);
////        AckStatus status = HelperMethods.executeCommand(command);
//        LOGGER.info("  Run command: {}", command);
//        ChannelExec copyResult = SSHConnect.setCommand(session, command);
//        AckStatus status = SSHConnect.checkAck(copyResult);
//        LOGGER.info("Copied {}: {}", Paths.get(sourceDir, filename).toString(), status.toString());
//        Assert.assertTrue(status.isOk());
//    }

//    private void copyFileToSSN3(Session session, String filename, String ssnIp) throws IOException, InterruptedException, JSchException {
//        String sourceDir = PropertiesResolver.getPythonFilesLocation();
//
//        LOGGER.info("Copying {}...", filename);
//        File file = new File(Paths.get(sourceDir, filename).toString());
//        Assert.assertTrue(file.exists());
//
//        session.connect();
//        Channel channel = session.openChannel("sftp");
//        channel.connect();
//        ChannelSftp c =(ChannelSftp)channel;
//        FileInputStream src = new FileInputStream(file);
//
//        try {
//
//            c.put(src, String.format("/home/%s/%s", ConfigPropertyValue.CLUSTER_OS_USERNAME, filename));
//
//        } catch (SftpException e) {
//            LOGGER.error(e);
//            Assert.assertFalse(true);
//        }
//    }

    private void copyFileToSSN33(ChannelSftp channel, String filename) throws IOException, InterruptedException, JSchException {
        String sourceDir = PropertiesResolver.getPythonFilesLocation();

        LOGGER.info("Copying {}...", filename);
        File file = new File(Paths.get(sourceDir, filename).toString());
        Assert.assertTrue(file.exists());

        FileInputStream src = new FileInputStream(file);

        try {

            channel.put(src, String.format("/home/%s/%s", ConfigPropertyValue.CLUSTER_OS_USERNAME, filename));

        } catch (SftpException e) {
            LOGGER.error(e);
            Assert.assertFalse(true);
        }
    }
    
    private void copyFileToNotebook(Session session, String filename, String ip) throws JSchException, IOException, InterruptedException {

    	String command = String.format(ScpCommands.copyToNotebookCommand,
    			"keys/"+ Paths.get(ConfigPropertyValue.getAccessKeyPrivFileNameSSN()).getFileName().toString(),
                filename,
                ConfigPropertyValue.CLUSTER_OS_USERNAME,
                ip);

    	LOGGER.info("Copying {}...", filename);
    	LOGGER.info("  Run command: {}", command);

        ChannelExec copyResult = SSHConnect.setCommand(session, command);
        AckStatus status = SSHConnect.checkAck(copyResult);

        LOGGER.info("Copied {}: {}", filename, status.toString());
        Assert.assertTrue(status.isOk());
    }

//    private void copyFileToNotebook2(Session session, String filename, String notebookIp) throws JSchException, IOException, InterruptedException {
//
//        String sourceDir = PropertiesResolver.getPythonFilesLocation();
//        LOGGER.info("Copying {}...", filename);
//        File file = new File(Paths.get(sourceDir, filename).toString());
//        Assert.assertTrue(file.exists());
//
//        session.connect();
//        Channel channel = session.openChannel("sftp");
//        channel.connect();
//        ChannelSftp c = (ChannelSftp) channel;
//        FileInputStream src = new FileInputStream(filename);
//
//        try {
//
//            c.put(src, String.format("/tmp/%s", ConfigPropertyValue.CLUSTER_OS_USERNAME, filename));
//
//        } catch (SftpException e) {
//            LOGGER.error(e);
//            Assert.assertFalse(true);
//        }
//
//    }
//    private void copyFileToNotebook22(ChannelSftp channelSftp, String filename) throws JSchException, IOException, InterruptedException {
//
//        String sourceDir = PropertiesResolver.getPythonFilesLocation();
//        LOGGER.info("Copying {}...", filename);
//        File file = new File(Paths.get(sourceDir, filename).toString());
//        Assert.assertTrue(file.exists());
//
//        FileInputStream src = new FileInputStream(filename);
//
//        try {
//
//            channelSftp.put(src, String.format("/tmp/%s", ConfigPropertyValue.CLUSTER_OS_USERNAME, filename));
//
//        } catch (SftpException e) {
//            LOGGER.error(e);
//            Assert.assertFalse(true);
//        }
//    }


    //TODO: think how to run locally through the putty
    // copy this files into project and copy from project to self service.
    private void testPython(String ssnIP, String noteBookIp, String emrName, String cluster_name)
            throws JSchException, IOException, InterruptedException {

    	String [] files = ConfigPropertyValue.get(ConfigPropertyValue.PYTHON_TEST_FILES).split(",");
        // it is assumed there should be 1 python file.
        String pyFileName = Arrays.stream(files).filter(file -> file.endsWith(".py")).findFirst().get();

        Session ssnSession = null;
        ChannelSftp channelSftp = null;
        try {
            LOGGER.info("Copying files to SSN {}...", ssnIP);
            ssnSession = SSHConnect.getSession(ConfigPropertyValue.CLUSTER_OS_USERNAME, ssnIP, 22);
            channelSftp = SSHConnect.getChannelSftp(ssnSession);

            for (String filename : files) {
                copyFileToSSN33(channelSftp, filename);
            }
        } finally {
            if(channelSftp != null && !channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
        }

        LOGGER.info("Copying files to Notebook {}...", noteBookIp);
        String command;
        AckStatus status;
//        Session noteBookSession = null;
//        ChannelSftp noteBookChannelSftp = null;
        try {
//             noteBookSession = SSHConnect.getSession(ConfigPropertyValue.CLUSTER_OS_USERNAME, noteBookIp, 22);
//             noteBookChannelSftp = SSHConnect.getChannelSftp(noteBookSession);

            for (String filename : files) {
            	copyFileToNotebook(ssnSession, filename, noteBookIp);
    		}

            LOGGER.info("Port forwarding from ssn {} to notebook {}...", ssnIP, noteBookIp);
            int assignedPort = ssnSession.setPortForwardingL(0, noteBookIp, 22);
            LOGGER.info("Port forwarded localhost:{} -> {}:22", assignedPort, noteBookIp);

            Session notebookSession = SSHConnect.getForwardedConnect(ConfigPropertyValue.CLUSTER_OS_USERNAME, noteBookIp, assignedPort);

            try {
                command = String.format(ScpCommands.runPythonCommand,
                        "/tmp/" +  pyFileName,
                        ConfigPropertyValue.getAwsRegion(),
                        getBucketName(),
                        cluster_name);
                LOGGER.info(String.format("Executing command %s...", command));

                ChannelExec runScript1 = SSHConnect.setCommand(notebookSession, "ls -la /tmp");
                LOGGER.info(SSHConnect.checkAck(runScript1).getMessage());
                ChannelExec runScript = SSHConnect.setCommand(notebookSession, command);
                status = SSHConnect.checkAck(runScript);
                LOGGER.info(status.getMessage());
                Assert.assertTrue(status.isOk(), "The python script execution wasn`t successful");

                LOGGER.info("Python script executed successfully ");
            }
            finally {
                if(notebookSession != null && notebookSession.isConnected()) {
                    LOGGER.info("Closing notebook session");
                    notebookSession.disconnect();
                }
            }
        }
        finally {
            if(ssnSession != null && ssnSession.isConnected()) {
                LOGGER.info("Closing ssn session");
                ssnSession.disconnect();
            }
        }
    }


    private String getSnnURL(String path) {
        return ssnURL + path;
    }

    private boolean waitForSSNService(Duration duration) throws InterruptedException {
        HttpRequest request = new HttpRequest();
        int actualStatus;
        long timeout = duration.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;

        while ((actualStatus = request.webApiGet(ssnURL, ContentType.TEXT).statusCode()) != HttpStatusCode.OK) {
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                break;
            }
            Thread.sleep(SSN_REQUEST_TIMEOUT);
        }

        if (actualStatus != HttpStatusCode.OK) {
            LOGGER.info("ERROR: Timeout has been expired for SSN available.");
            LOGGER.info("  timeout is {}", duration);
            return false;
        } else {
    		LOGGER.info("Current status code for SSN is {}", actualStatus);
    	}
        
        return true;
    }

    private int waitWhileStatus(String url, String token, int status, Duration duration)
            throws InterruptedException {
    	LOGGER.info("Waiting until status code {} with URL {} with token {}", status, url, token);
        HttpRequest request = new HttpRequest();
        int actualStatus;
        long timeout = duration.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;

        while ((actualStatus = request.webApiGet(url, token).getStatusCode()) == status) {
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                break;
            }
            Thread.sleep(SSN_REQUEST_TIMEOUT);
        }

        if (actualStatus == status) {
            LOGGER.info("ERROR: Timeout has been expired for request.");
            LOGGER.info("  URL is {}", url);
            LOGGER.info("  token is {}", token);
            LOGGER.info("  status is {}", status);
            LOGGER.info("  timeout is {}", duration);
    	} else {
    		LOGGER.info("Current status code for {} is ",url, actualStatus);
    	}

        return actualStatus;
    }

    private String getNotebookStatus(JsonPath json, String notebookName) {
    	List<Map<String, String>> notebooks = json
				.param("name", notebookName)
				.getList("findAll { notebook -> notebook.exploratory_name == name }");
        if (notebooks == null || notebooks.size() != 1) {
        	return "";
        }
        Map<String, String> notebook = notebooks.get(0);
        String status = notebook.get("status");
        return (status == null ? "" : status);
    }

    private String waitWhileNotebookStatus(String url, String token, String notebookName, String status, Duration duration)
            throws InterruptedException {
    	LOGGER.info("Waiting until status {} with URL {} with token {} for notebook {}",status, url, token, notebookName);
        HttpRequest request = new HttpRequest();
        String actualStatus;
        long timeout = duration.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;

        while ((actualStatus = getNotebookStatus(request.webApiGet(url, token)
        											.getBody()
        											.jsonPath(), notebookName)).equals(status)) {
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                break;
            }
            Thread.sleep(SSN_REQUEST_TIMEOUT);
        }

        if (actualStatus.contains(status)) {
            LOGGER.info("ERROR: Timeout has been expired for request.");
            LOGGER.info("  URL is {}", url);
            LOGGER.info("  token is {}", token);
            LOGGER.info("  status is {}", status);
            LOGGER.info("  timeout is {}", duration);
        } else {
        	LOGGER.info("Current state for Notebook {} is {}", notebookName, actualStatus );
        }
        
        return actualStatus;
    }
    
    private String getEmrStatus(JsonPath json, String notebookName, String computationalName) {
    	List<Map<String, List<Map<String, String>>>> notebooks = json
				.param("name", notebookName)
				.getList("findAll { notebook -> notebook.exploratory_name == name }");
        if (notebooks == null || notebooks.size() != 1) {
        	return "";
        }
        List<Map<String, String>> resources = notebooks.get(0)
        		.get("computational_resources");
        for (Map<String, String> resource : resources) {
            String comp = resource.get("computational_name");
            if (comp != null && comp.equals(computationalName)) {
            	return resource.get("status");
            }
		}
		return "";
    }
    
    private String waitWhileEmrStatus(String url, String token, String notebookName, String computationalName, String status, Duration duration)
            throws InterruptedException {
    	LOGGER.info("Waiting until status {} with URL {} with token {} for computational {} on notebook ", status, url, token, computationalName, notebookName);
        HttpRequest request = new HttpRequest();
        String actualStatus;
        long timeout = duration.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;

        while ((actualStatus = getEmrStatus(request.webApiGet(url, token)
        											.getBody()
        											.jsonPath(), notebookName, computationalName)).equals(status)) {
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                break;
            }
            Thread.sleep(SSN_REQUEST_TIMEOUT);
        }

        if (actualStatus.contains(status)) {
            LOGGER.info("ERROR: Timeout has been expired for request.");
            LOGGER.info("  URL is {}",  url);
            LOGGER.info("  token is {}", token);
            LOGGER.info("  status is {}", status);
            LOGGER.info("  timeout is {}", duration);
        } else {
        	LOGGER.info("Current state for EMR {} on notebook {} is ", computationalName, notebookName, actualStatus);
        }
        
        return actualStatus;
    }
    
}
