package com.epam.dlab.automation.test;

import com.amazonaws.services.ec2.model.Instance;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.testng.Assert.assertTrue;

@Test(singleThreaded=true)
public class TestServices {

    private final static Logger LOGGER = LogManager.getLogger(TestServices.class);
    private final static long SSN_REQUEST_TIMEOUT = 10000; 

	private String serviceBaseName;
    private String ssnURL;
    private String publicSsnIp;
    private String privateSsnIp;

    private String ssnIpForTest;

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


    @Test(priority = 1, dependsOnMethods = "runJenkins")
    public void runSsnLogin() throws Exception {
        LOGGER.info("Test Started");
        testLoginSsnService();
        LOGGER.info("Test Finished");
    }


    @Test(priority = 2, dependsOnMethods = "runSsnLogin")
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

    }
    
    private void testLoginSsnService() throws Exception {
    	
    	//ssnURL = "http://ec2-35-162-89-115.us-west-2.compute.amazonaws.com";

        LOGGER.info("Check status of SSN node on Amazon: {}", serviceBaseName);
        Instance ssnInstance = AmazonHelper.getInstance(serviceBaseName + "-ssn");
        publicSsnIp = ssnInstance.getPublicIpAddress();
        LOGGER.info("Public IP is: {}", publicSsnIp);
        privateSsnIp = ssnInstance.getPrivateIpAddress();
        LOGGER.info("Private IP is: {}", privateSsnIp);
        ssnIpForTest = PropertiesResolver.DEV_MODE ? publicSsnIp : privateSsnIp;
        AmazonHelper.checkAmazonStatus(serviceBaseName + "-ssn", AmazonInstanceState.RUNNING.value());
        LOGGER.info("Amazon instance state is running");
        
        LOGGER.info("2. Waiting for SSN service ...");
        Assert.assertEquals(waitForSSNService(ConfigPropertyValue.getTimeoutNotebookCreate()), true, "SSN service was not started");
        LOGGER.info("   SSN service is available");
        
        
        LOGGER.info("3. Check login");
        final String ssnLoginURL = getSnnURL(ApiPath.LOGIN);
        LOGGER.info("   SSN login URL is {}", ssnLoginURL);
        
        if (!ConfigPropertyValue.isRunModeLocal()) {
        	LoginDto notIAMUserRequestBody = new LoginDto(ConfigPropertyValue.getNotIAMUsername(), ConfigPropertyValue.getNotIAMPassword(), "");
        	Response responseNotIAMUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notIAMUserRequestBody);
        	Assert.assertEquals(responseNotIAMUser.statusCode(), HttpStatusCode.Unauthorized);
        	Assert.assertEquals(responseNotIAMUser.getBody().asString(), "Please contact AWS administrator to create corresponding IAM User");
        }
 		
        LoginDto notDLABUserRequestBody = new LoginDto(ConfigPropertyValue.getNotDLabUsername(), ConfigPropertyValue.getNotDLabPassword(), "");
        Response responseNotDLABUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notDLABUserRequestBody);
        Assert.assertEquals(responseNotDLABUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotDLABUser.getBody().asString(), "Username or password are not valid");
        
        if (!ConfigPropertyValue.isRunModeLocal()) {
        	LoginDto forActivateAccessKey = new LoginDto(ConfigPropertyValue.getUsername(), ".", "");
        	Response responseForActivateAccessKey = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, forActivateAccessKey);
        	Assert.assertEquals(responseForActivateAccessKey.statusCode(), HttpStatusCode.Unauthorized);
        	Assert.assertEquals(responseForActivateAccessKey.getBody().asString(), "Username or password are not valid");
        }
        
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
        //publicSsnIp = "35.164.76.52";

        final String nodePrefix = ConfigPropertyValue.getUsernameSimple();
        final String amazonNodePrefix = serviceBaseName + "-" + nodePrefix;

        RestAssured.baseURI = ssnURL;
        final String ssnUploadKeyURL = getSnnURL(ApiPath.UPLOAD_KEY);

        String token = ssnLoginAndKeyUpload(nodePrefix, amazonNodePrefix);

        final String ssnExpEnvURL = getSnnURL(ApiPath.EXP_ENVIRONMENT);
        LOGGER.info("   SSN exploratory environment URL is {}", ssnExpEnvURL);
        final String ssnProUserResURL = getSnnURL(ApiPath.PROVISIONED_RES);
        LOGGER.info("   SSN provisioned user resources URL is {}", ssnProUserResURL);

        runTestsInNotebooks(nodePrefix, amazonNodePrefix, token, ssnExpEnvURL, ssnProUserResURL);

    }

    private String ssnLoginAndKeyUpload(String nodePrefix, String amazonNodePrefix) throws Exception {

        LoginDto testUserRequestBody = new LoginDto(ConfigPropertyValue.getUsername(), ConfigPropertyValue.getPassword(), "");

        LOGGER.info("5. Login as {} ...", ConfigPropertyValue.getUsername());

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

        Docker.checkDockerStatus(nodePrefix + "_create_edge_", ssnIpForTest);
        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-edge", AmazonInstanceState.RUNNING.value());


        final String ssnExpEnvURL = getSnnURL(ApiPath.EXP_ENVIRONMENT);
        LOGGER.info("   SSN exploratory environment URL is {}", ssnExpEnvURL);
        final String ssnProUserResURL = getSnnURL(ApiPath.PROVISIONED_RES);
        LOGGER.info("   SSN provisioned user resources URL is {}", ssnProUserResURL);

        return token;
    }

    private void runTestsInNotebooks(String nodePrefix, String amazonNodePrefix, String token, String ssnExpEnvURL, String ssnProUserResURL) throws Exception {

        Map<String, String> notebooks = new HashMap<>();
        notebooks.put("jupyter-notebook.json", PropertiesResolver.getJupyterFilesLocation());
        notebooks.put("zeppelin-notebook.json",PropertiesResolver.getZeppelinFilesLocation());
        notebooks.put("rstudio-notebook.json",PropertiesResolver.getRstudioFilesLocation());
        ExecutorService executor =  Executors.newFixedThreadPool(3);
        List<FutureTask<Boolean>> futureTasks = new ArrayList();

        for (String notebookConfig: notebooks.keySet()) {
            FutureTask runScenarioTask = new FutureTask<Boolean>(new PythonTestCallable(notebookConfig, nodePrefix, amazonNodePrefix, token, ssnExpEnvURL, ssnProUserResURL, notebooks.get(notebookConfig)));
            futureTasks.add(runScenarioTask);
            executor.execute(runScenarioTask);
        }

        while (true) {
            boolean done = true;
            done = allScenariosDone(futureTasks);
            if (done) {
                verifyResults(futureTasks);
                executor.shutdown();
                return;
            } else {
                Thread.sleep(1000 * 60);
            }
        }
    }

    private void verifyResults(List<FutureTask<Boolean>> futureTasks) throws InterruptedException, ExecutionException {
        for (FutureTask<Boolean> ft : futureTasks) {
            assertTrue(ft.get(), ft.get().toString());
        }
    }

    private boolean allScenariosDone(List<FutureTask<Boolean>> futureTasks) {
        boolean done = true;
        for (FutureTask<Boolean> ft : futureTasks) {
            if(!ft.isDone()) {
                done = ft.isDone();
            }
        }
        return done;
    }

    private void stopEnvironment(String nodePrefix, String amazonNodePrefix, String token, String ssnProUserResURL, String notebookConfig, String testNoteBookName, String emrName) throws Exception {
        String gettingStatus;
        LOGGER.info("8. Notebook " + notebookConfig + " will be stopped ...");
        final String ssnStopNotebookURL = getSnnURL(ApiPath.getStopNotebookUrl(testNoteBookName));
        LOGGER.info("   SSN stop notebook URL is {}", ssnStopNotebookURL);

        Response responseStopNotebook = new HttpRequest().webApiDelete(ssnStopNotebookURL,
                ContentType.JSON, token);
        LOGGER.info("   responseStopNotebook.getBody() is {}", responseStopNotebook.getBody().asString());
        Assert.assertEquals(responseStopNotebook.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, testNoteBookName, "stopping", ConfigPropertyValue.getTimeoutNotebookShutdown());
        if (!gettingStatus.contains("stopped"))
            throw new Exception("Notebook " + testNoteBookName + " has not been stopped");
        LOGGER.info("   Notebook {} has been stopped", testNoteBookName);
        gettingStatus = getEmrStatus(
                new HttpRequest()
                        .webApiGet(ssnProUserResURL, token)
                        .getBody()
                        .jsonPath(),
                testNoteBookName, emrName);

        if (!gettingStatus.contains("terminated"))
            throw new Exception("Computational resources has not been terminated for Notebook " + testNoteBookName);
        LOGGER.info("   Computational resources has been terminated for Notebook {}", testNoteBookName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + testNoteBookName + "-" + emrName, AmazonInstanceState.TERMINATED.value());
        Docker.checkDockerStatus(nodePrefix + "_stop_exploratory_NotebookAutoTest", ssnIpForTest);
    }

    /**
     *
     * @param testNoteBookName
     * @param notebookConfig
     * @param nodePrefix
     * @param amazonNodePrefix
     * @param token
     * @param ssnExpEnvURL
     * @param ssnProUserResURL
     * @return notebook IP
     * @throws Exception
     */
    private String  createNotebook(String testNoteBookName, String notebookConfig, String nodePrefix, String amazonNodePrefix, String token, String ssnExpEnvURL, String ssnProUserResURL) throws Exception {
        LOGGER.info("6. Notebook " +  notebookConfig + " will be created ...");

        CreateNotebookDto createNoteBookRequest =
                NodeReader.readNode(
                        Paths.get(PropertiesResolver.getClusterConfFileLocation(), notebookConfig).toString(),
                        CreateNotebookDto.class);

            createNoteBookRequest.setName(testNoteBookName);

            Response responseCreateNotebook = new HttpRequest().webApiPut(ssnExpEnvURL, ContentType.JSON,
                    createNoteBookRequest, token);
            LOGGER.info("   responseCreateNotebook.getBody() is {}", responseCreateNotebook.getBody().asString());
            Assert.assertEquals(responseCreateNotebook.statusCode(), HttpStatusCode.OK);

        String gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, testNoteBookName, "creating", ConfigPropertyValue.getTimeoutNotebookCreate());
        if (!gettingStatus.contains("running")) {
            LOGGER.error("Notebook {} is in state {}", testNoteBookName, gettingStatus);
            throw new Exception("Notebook " + testNoteBookName + " has not been created");
        }
        LOGGER.info("   Notebook {} has been created", testNoteBookName);

        AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-nb-" + testNoteBookName, AmazonInstanceState.RUNNING.value());
        Docker.checkDockerStatus(nodePrefix + "_create_exploratory_NotebookAutoTest", ssnIpForTest);

        //get notebook IP
        String notebookIp = AmazonHelper.getInstance(amazonNodePrefix + "-nb-" + testNoteBookName)
                .getPrivateIpAddress();

        return notebookIp;
    }

    private void createEMR(String testNoteBookName, String emrName, String nodePrefix, String amazonNodePrefix, String token, String ssnProUserResURL) throws Exception {
        String gettingStatus;
        LOGGER.info("7. EMR will be deployed ...");
        final String ssnCompResURL = getSnnURL(ApiPath.COMPUTATIONAL_RES);
        LOGGER.info("   SSN computational resources URL is {}", ssnCompResURL);

        DeployEMRDto deployEMR =
                NodeReader.readNode(
                        Paths.get(PropertiesResolver.getClusterConfFileLocation(), "EMR.json").toString(),
                        DeployEMRDto.class);

        //TODO: add parameter for switching from regular ec2 instances to spot instances
        DeployEMRDto deployEMRSpot40 =
                NodeReader.readNode(
                        Paths.get(PropertiesResolver.getClusterConfFileLocation(), "EMR_spot.json").toString(),
                        DeployEMRDto.class);

        deployEMR.setName(emrName);
        deployEMR.setNotebook_name(testNoteBookName);
        LOGGER.info("EMR = {}", deployEMR);
        Response responseDeployingEMR = new HttpRequest().webApiPut(ssnCompResURL, ContentType.JSON,
                deployEMR, token);
        LOGGER.info("   responseDeployingEMR.getBody() is {}", responseDeployingEMR.getBody().asString());
        Assert.assertEquals(responseDeployingEMR.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, testNoteBookName, emrName, "creating", ConfigPropertyValue.getTimeoutEMRCreate());
        if(!ConfigPropertyValue.isRunModeLocal()) {
            if (!gettingStatus.contains("configuring"))
                throw new Exception("EMR " + emrName + " has not been deployed");
            LOGGER.info("   EMR {} has been deployed", emrName);

            AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + testNoteBookName + "-" + emrName, AmazonInstanceState.RUNNING.value());
            Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", ssnIpForTest);
        }
        LOGGER.info("   Waiting until EMR has been configured ...");

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, testNoteBookName, emrName, "configuring", ConfigPropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR " + emrName + " has not been configured");
        LOGGER.info("   EMR {} has been configured", emrName);

        if(!ConfigPropertyValue.isRunModeLocal()) {
            AmazonHelper.checkAmazonStatus(amazonNodePrefix + "-emr-" + testNoteBookName + "-" + emrName, AmazonInstanceState.RUNNING.value());
            Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", ssnIpForTest);
        }

        LOGGER.info("   Check bucket {}", getBucketName());
        AmazonHelper.printBucketGrants(getBucketName());
    }

    private void copyTestDataIntoTestBucket(String emrName, String clusterName) throws Exception {
        Session ssnSession = null;
        ChannelSftp channelSftp = null;
        try {
            LOGGER.info("Copying test data copy scripts  to SSN {}...", ssnIpForTest);
            ssnSession = SSHConnect.getSession(ConfigPropertyValue.getClusterOsUser(), ssnIpForTest, 22);
            channelSftp = SSHConnect.getChannelSftp(ssnSession);

            copyFileToSSN(channelSftp, PropertiesResolver.getNotebookTestDataCopyScriptLocation());

            executePythonScript2(ssnSession, clusterName, new File(PropertiesResolver.getNotebookTestDataCopyScriptLocation()).getName());

        }  finally {
        if(channelSftp != null && !channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if(ssnSession != null && !ssnSession.isConnected()) {
            ssnSession.disconnect();
        }
    }
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

    private void copyFileToSSN(ChannelSftp channel, String filenameWithPath) throws IOException, InterruptedException, JSchException {
        LOGGER.info("Copying {}...", filenameWithPath);
        File file = new File(filenameWithPath);
        assertTrue(file.exists());

        FileInputStream src = new FileInputStream(file);

        try {

            channel.put(src, String.format("/home/%s/%s", ConfigPropertyValue.getClusterOsUser(), file.getName()));

        } catch (SftpException e) {
            LOGGER.error(e);
            assertTrue(false);
        }
    }
    
    private void copyFileToNotebook(Session session, String filename, String ip) throws JSchException, IOException, InterruptedException {

    	String command = String.format(ScpCommands.copyToNotebookCommand,
    			"keys/"+ Paths.get(ConfigPropertyValue.getAccessKeyPrivFileName()).getFileName().toString(),
                filename,
                ConfigPropertyValue.getClusterOsUser(),
                ip);

    	LOGGER.info("Copying {}...", filename);
    	LOGGER.info("  Run command: {}", command);

        ChannelExec copyResult = SSHConnect.setCommand(session, command);
        AckStatus status = SSHConnect.checkAck(copyResult);

        LOGGER.info("Copied {}: {}", filename, status.toString());
        assertTrue(status.isOk());
    }

    private void testPythonScripts(String ssnIP, String noteBookIp, String emrName, String clusterName, File notebookDirectory)
            throws JSchException, IOException, InterruptedException {
    	LOGGER.info("Python tests will be started ...");
    	if (ConfigPropertyValue.isRunModeLocal()) {
    		LOGGER.info("  tests are skipped");
    		return;
    	}

        assertTrue(notebookDirectory.exists());
        assertTrue(notebookDirectory.isDirectory());

    	String [] files = notebookDirectory.list();
    	assertTrue(files.length == 1);
        assertTrue(files[0].endsWith(".py"));
        // it is assumed there should be 1 python file.
        String notebookTestFile = files[0];

        Session ssnSession = null;
        ChannelSftp channelSftp = null;
        try {
            LOGGER.info("Copying files to SSN {}...", ssnIP);
            ssnSession = SSHConnect.getSession(ConfigPropertyValue.getClusterOsUser(), ssnIP, 22);
            channelSftp = SSHConnect.getChannelSftp(ssnSession);

            copyFileToSSN(channelSftp, Paths.get(notebookDirectory.getAbsolutePath(), notebookTestFile).toString());
        } finally {
            if(channelSftp != null && !channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
        }

        LOGGER.info("Copying files to Notebook {}...", noteBookIp);
        String command;
        AckStatus status;

        try {
            for (String filename : files) {
            	copyFileToNotebook(ssnSession, filename, noteBookIp);
    		}

            LOGGER.info("Port forwarding from ssn {} to notebook {}...", ssnIP, noteBookIp);
            int assignedPort = ssnSession.setPortForwardingL(0, noteBookIp, 22);
            LOGGER.info("Port forwarded localhost:{} -> {}:22", assignedPort, noteBookIp);

            executePythonScript(noteBookIp, clusterName, notebookTestFile, assignedPort);
        }
        finally {
            if(ssnSession != null && ssnSession.isConnected()) {
                LOGGER.info("Closing ssn session");
                ssnSession.disconnect();
            }
        }
    }

    private void executePythonScript(String Ip, String cluster_name, String notebookTestFile, int assignedPort) throws JSchException, IOException, InterruptedException {
        String command;
        AckStatus status;
        Session session = SSHConnect.getForwardedConnect(ConfigPropertyValue.getClusterOsUser(), Ip, assignedPort);

        try {
            command = String.format(ScpCommands.runPythonCommand,
                    "/tmp/" +  notebookTestFile,
                    getBucketName(),
                    cluster_name,
                    ConfigPropertyValue.getClusterOsUser());
            LOGGER.info(String.format("Executing command %s...", command));

            ChannelExec runScript = SSHConnect.setCommand(session, command);
            status = SSHConnect.checkAck(runScript);
            LOGGER.info("Script execution status message {} and status code {}", status.getMessage(), status.getStatus());
            assertTrue(status.isOk(), "The python script execution wasn`t successful");

            LOGGER.info("Python script executed successfully ");
        }
        finally {
            if(session != null && session.isConnected()) {
                LOGGER.info("Closing notebook session");
                session.disconnect();
            }
        }
    }

    private void executePythonScript2(Session ssnSession, String clusterName, String notebookTestFile) throws JSchException, IOException, InterruptedException {
        String command;
        AckStatus status;

            command = String.format(ScpCommands.runPythonCommand2,
                    String.format("/home/%s/%s", ConfigPropertyValue.getClusterOsUser(), notebookTestFile),
                    getBucketName());
            LOGGER.info(String.format("Executing command %s...", command));

            ChannelExec runScript = SSHConnect.setCommand(ssnSession, command);
            status = SSHConnect.checkAck(runScript);
            LOGGER.info("Script execution status message {} and code {}", status.getMessage(), status.getStatus());
            assertTrue(status.isOk(), "The python script execution wasn`t successful on : " + clusterName);

            LOGGER.info("Python script executed successfully ");
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


    class PythonTestCallable implements Callable<Boolean> {

        private String notebookConfig, nodePrefix, amazonNodePrefix, token, ssnExpEnvURL, ssnProUserResURL, notebookFileLocation;

        public PythonTestCallable(String notebookConfig, String nodePrefix, String amazonNodePrefix, String token, String ssnExpEnvURL, String ssnProUserResURL, String notebookFileLocation) {
            this.notebookConfig = notebookConfig;
            this.nodePrefix = nodePrefix;
            this.amazonNodePrefix = amazonNodePrefix;
            this.token = token;
            this.ssnExpEnvURL = ssnExpEnvURL;
            this.ssnProUserResURL = ssnProUserResURL;
            this.notebookFileLocation = notebookFileLocation;
        }

        @Override
        public Boolean call() throws Exception {

            String notebookTemplateNamePrefix = notebookConfig.substring(0, notebookConfig.indexOf(".") > 0 ? notebookConfig.indexOf(".") : notebookConfig.length() - 1);
            String notebookTemplateName = TestNamingHelper.generateRandomValue(notebookTemplateNamePrefix);

//          String testNoteBookName = "NotebookAutoTest_R-Studio_20170516122058";
//          String testNoteBookName = "NotebookAutoTest_Zeppelin_2017051633454";
            String testNoteBookName = "Notebook" + notebookTemplateName;

//          String emrName = "eimrAutoTest_R-Studio_20170516125150";
//          String emrName = "eimrAutoTest_Zeppelin_2017051641947";
            String emrName = "eimr" + notebookTemplateName;

            String notebookIp = createNotebook(testNoteBookName, notebookConfig, nodePrefix, amazonNodePrefix, token, ssnExpEnvURL, ssnProUserResURL);
            createEMR(testNoteBookName, emrName, nodePrefix, amazonNodePrefix, token, ssnProUserResURL);

            String emrClusterName = getEmrClusterName(amazonNodePrefix + "-emr-" + testNoteBookName + "-" + emrName);
            if(!ConfigPropertyValue.isRunModeLocal()) {
                copyTestDataIntoTestBucket(emrName, emrClusterName);
            }

            testPythonScripts(ssnIpForTest, notebookIp, emrName, emrClusterName, new File(notebookFileLocation));

            stopEnvironment(nodePrefix, amazonNodePrefix, token, ssnProUserResURL, notebookConfig, testNoteBookName, emrName);

            return true;
        }
    }

}
