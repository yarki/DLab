package AutomationTest;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Tag;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import AmazonHelper.Amazon;
import AmazonHelper.AmazonInstanceState;
import DataModel.CreateNotebookDto;
import DataModel.DeployEMRDto;
import DataModel.LoginDto;
import DockerHelper.AckStatus;
import DockerHelper.Docker;
import DockerHelper.SSHConnect;
import Infrastucture.HttpRequest;
import Repository.ContentType;
import Repository.HttpStatusCode;
import Repository.Path;
import ServiceCall.JenkinsCall;

@Test(singleThreaded=true)
public class TestServices {
    private final static Logger logger = Logger.getLogger(TestServices.class.getName());
    private final static long SSN_REQUEST_TIMEOUT = 10000; 

	private String serviceBaseName;
    private String ssnURL;
    private String publicIp;

    @BeforeClass
    public void Setup() throws InterruptedException {
        // loading log4j.xml file
        DOMConfigurator.configure("log4j.xml");

        // Load properties
        PropertyValue.getJenkinsJobURL();
    }
    
    @AfterClass
    public void Cleanup() throws InterruptedException {
    }

    @Test
    public void runTests() throws Exception {
    	testJenkinsJob();
    	testLogin();
    	testDLabScenario();
    }
    
    private void testJenkinsJob() throws Exception {

        /* System.out.println("1. Jenkins Job will be started ...");
       
        JenkinsCall jenkins = new JenkinsCall(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword());
        String buildNumber = jenkins.runJenkinsJob(PropertyValue.getJenkinsJobURL());
        System.out.println("   Jenkins Job has been completed"); */

        System.out.println("1. Looking for last Jenkins Job ...");
        JenkinsCall jenkins = new JenkinsCall();
        String buildNumber = jenkins.getJenkinsJob();
        System.out.println("   Jenkins Job found:");
        System.out.println("Build number is: " + buildNumber);
        
        ssnURL = jenkins.getSsnURL().replaceAll(" ", "");
        serviceBaseName = jenkins.getServiceBaseName().replaceAll(" ", "");
        Assert.assertNotNull(ssnURL, "Jenkins URL was not generated");
        Assert.assertNotNull(serviceBaseName, "Service BaseName was not generated");
        System.out.println("JenkinsURL is: " + ssnURL);
        System.out.println("ServiceBaseName is: " + serviceBaseName);

        System.out.println("Check status of SSN node on Amazon:");
        Instance ssnInstance = Amazon.getInstance(serviceBaseName + "-ssn");
        InstanceState instanceState = ssnInstance.getState();
        publicIp = ssnInstance.getPublicIpAddress();
        System.out.println("Public Ip is: " + publicIp);
        Assert.assertEquals(instanceState.getName(), AmazonInstanceState.RUNNING,
                            "Amazon instance state is not running");
        System.out.println("Amazon instance state is running");
    }
    
    private void testLogin() throws Exception {
    	
    	//ssnURL = "http://ec2-35-162-89-115.us-west-2.compute.amazonaws.com";
        
        System.out.println("2. Waiting for SSN service ...");
        Assert.assertEquals(waitForSSNService(PropertyValue.getTimeoutNotebookCreate()), true, "SSN service was not started");
        System.out.println("   SSN service is available");
        
        
        System.out.println("3. Check login");
        final String ssnLoginURL = getSnnURL(Path.LOGIN);
        System.out.println("   SSN login URL is " + ssnLoginURL);
        
        LoginDto notIAMUserRequestBody = new LoginDto(PropertyValue.getNotIAMUsername(), PropertyValue.getNotIAMPassword(), "");
        Response responseNotIAMUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notIAMUserRequestBody);
        Assert.assertEquals(responseNotIAMUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotIAMUser.getBody().asString(), "Please contact AWS administrator to create corresponding IAM User");
 		
        LoginDto notDLABUserRequestBody = new LoginDto(PropertyValue.getNotDLabUsername(), PropertyValue.getNotDLabPassword(), "");
        Response responseNotDLABUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, notDLABUserRequestBody);
        Assert.assertEquals(responseNotDLABUser.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseNotDLABUser.getBody().asString(), "Username or password are not valid");
        
        LoginDto forActivateAccessKey = new LoginDto(PropertyValue.getUsername(), ".", "");
        Response responseForActivateAccessKey = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, forActivateAccessKey);
        Assert.assertEquals(responseForActivateAccessKey.statusCode(), HttpStatusCode.Unauthorized);
        Assert.assertEquals(responseForActivateAccessKey.getBody().asString(), "Username or password are not valid");
        
        LoginDto testUserLogin = new LoginDto(PropertyValue.getUsername(), PropertyValue.getPassword(), "");
        Response responseTestUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, testUserLogin);
        Assert.assertEquals(responseTestUser.statusCode(), HttpStatusCode.OK);
 		
        
        System.out.println("4. Check logout");
        final String ssnlogoutURL = getSnnURL(Path.LOGOUT);
        System.out.println("   SSN logout URL is " + ssnlogoutURL);
        
        Response responseLogout = new HttpRequest().webApiPost(ssnlogoutURL, ContentType.ANY);
        System.out.println("responseLogout.statusCode() is " + responseLogout.statusCode());
        Assert.assertEquals(responseLogout.statusCode(), HttpStatusCode.Unauthorized/*Replace to HttpStatusCode.OK when EPMCBDCCSS-938 will be fixed and merged*/);
    }

    private void testDLabScenario() throws Exception {

    	//ssnURL = "http://ec2-35-164-76-52.us-west-2.compute.amazonaws.com";
        //serviceBaseName = "autotest_jan11";
        //publicIp = "35.164.76.52";

        String gettingStatus;
        String noteBookName = "Notebook" + HelperMethods.generateRandomValue();
        String emrName = "eimr" + HelperMethods.generateRandomValue();
        
        final String nodePrefix = PropertyValue.getUsernameSimple();
        final String amazonNodePrefix = serviceBaseName + "-" + nodePrefix;

        RestAssured.baseURI = ssnURL;
        LoginDto testUserRequestBody = new LoginDto(PropertyValue.getUsername(), PropertyValue.getPassword(), "");

        
        System.out.println("5. Logging user in...");

        final String ssnLoginURL = getSnnURL(Path.LOGIN);
        System.out.println("   SSN login URL is " + ssnLoginURL);

        final String ssnUploadKeyURL = getSnnURL(Path.UPLOAD_KEY);
        System.out.println("   SSN upload key URL is " + ssnUploadKeyURL);

        Response responseTestUser = new HttpRequest().webApiPost(ssnLoginURL, ContentType.JSON, testUserRequestBody);
        Assert.assertEquals(HttpStatusCode.OK, responseTestUser.getStatusCode(), "Failed to login");
        String token = responseTestUser.getBody().asString();
        System.out.println("   Logged in. Obtained token: " + token);

        
        System.out.println("5.a Checking for user Key...");
        Response respCheckKey = new HttpRequest().webApiGet(ssnUploadKeyURL, token);

        if(respCheckKey.getStatusCode() == HttpStatusCode.NotFound) {
            System.out.println("5.b Upload Key will be started ...");

            Response respUploadKey = new HttpRequest().webApiPost(ssnUploadKeyURL, ContentType.FORMDATA, token);
            System.out.println("   respUploadKey.getBody() is " + respUploadKey.getBody().asString());

            Assert.assertEquals(respUploadKey.statusCode(), HttpStatusCode.OK, "Upload key is not correct");
            int responseCodeAccessKey = waitWhileStatus(ssnUploadKeyURL, token, HttpStatusCode.Accepted, PropertyValue.getTimeoutUploadKey());
            System.out.println("   Upload Key has been completed");
            System.out.println("responseAccessKey.statusCode() is " + responseCodeAccessKey);
            Assert.assertEquals(responseCodeAccessKey, HttpStatusCode.OK, "Upload key is not correct");
        } else if (respCheckKey.getStatusCode() == HttpStatusCode.OK){
            System.out.println("   Key has been uploaded already");
        } else {
            Assert.assertEquals(200, respCheckKey.getStatusCode(), "Failed to check User Key.");
        }

        Docker.checkDockerStatus(nodePrefix + "_create_edge_", publicIp);
        Amazon.checkAmazonStatus(amazonNodePrefix + "-edge", AmazonInstanceState.RUNNING);

        
        System.out.println("7. Notebook will be created ...");
        final String ssnExpEnvURL = getSnnURL(Path.EXP_ENVIRONMENT);
        System.out.println("   SSN exploratory environment URL is " + ssnExpEnvURL);
        final String ssnProUserResURL = getSnnURL(Path.PROVISIONED_RES);
        System.out.println("   SSN provisioned user resources URL is " + ssnProUserResURL);

        CreateNotebookDto createNoteBookRequest = new CreateNotebookDto();
        createNoteBookRequest.setImage("docker.epmc-bdcc.projects.epam.com/dlab-aws-jupyter");
        createNoteBookRequest.setTemplateName("Jupyter 1.5");
        createNoteBookRequest.setName(noteBookName);
        createNoteBookRequest.setShape("r3.xlarge");
        createNoteBookRequest.setVersion("jupyter-1.6");
        Response responseCreateNotebook = new HttpRequest().webApiPut(ssnExpEnvURL, ContentType.JSON,
                                                                      createNoteBookRequest, token);
        System.out.println("   responseCreateNotebook.getBody() is " + responseCreateNotebook.getBody().asString());
        Assert.assertEquals(responseCreateNotebook.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "creating", PropertyValue.getTimeoutNotebookCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook " + noteBookName + " has not been created");
        System.out.println("   Notebook " + noteBookName + " has been created");

        Amazon.checkAmazonStatus(amazonNodePrefix + "-nb-" + noteBookName, AmazonInstanceState.RUNNING);
        Docker.checkDockerStatus(nodePrefix + "_create_exploratory_NotebookAutoTest", publicIp);
        
        //get notebook IP
        String notebookIp = Amazon.getInstance(amazonNodePrefix + "-nb-" + noteBookName)
        		.getPrivateIpAddress();
        
        
        System.out.println("8. EMR will be deployed ...");
        final String ssnCompResURL = getSnnURL(Path.COMPUTATIONAL_RES);
        System.out.println("   SSN computational resources URL is " + ssnCompResURL);
        
        DeployEMRDto deployEMR = new DeployEMRDto();
        final String emrVersion="emr-5.2.0";
        deployEMR.setEmr_instance_count("2"); // TODO: Set to 3
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMR = new HttpRequest().webApiPut(ssnCompResURL, ContentType.JSON,
                                                                    deployEMR, token);
        System.out.println("   responseDeployingEMR.getBody() is " + responseDeployingEMR.getBody().asString());
        Assert.assertEquals(responseDeployingEMR.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrName, "creating", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("configuring"))
            throw new Exception("EMR " + emrName + " has not been deployed");
        System.out.println("   EMR " + emrName + " has been deployed");

        Amazon.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName, AmazonInstanceState.RUNNING);
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);
        
        System.out.println("   Waiting until EMR has been configured ...");

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrName, "configuring", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR " + emrName + " has not been configured");
        System.out.println("   EMR " + emrName + " has been configured");

        Amazon.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName, AmazonInstanceState.RUNNING);
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);

        System.out.println("   Check bucket " + getBucketName());
        Amazon.printBucketGrants(getBucketName());
        
        //run python script
        testPython(publicIp, notebookIp, emrName, getEmrClusterName(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName));

        
        System.out.println("9. Notebook will be stopped ...");
        final String ssnStopNotebookURL = getSnnURL(Path.getStopNotebookUrl(noteBookName));
        System.out.println("   SSN stop notebook URL is " + ssnStopNotebookURL);

        Response responseStopNotebook = new HttpRequest().webApiDelete(ssnStopNotebookURL,
                                                                       ContentType.JSON, token);
        System.out.println("   responseStopNotebook.getBody() is " + responseStopNotebook.getBody().asString());
        Assert.assertEquals(responseStopNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "stopping", PropertyValue.getTimeoutNotebookShutdown());
        if (!gettingStatus.contains("stopped"))
            throw new Exception("Notebook " + noteBookName + " has not been stopped");
        System.out.println("   Notebook " + noteBookName + " has been stopped");
        gettingStatus = getEmrStatus(
        					new HttpRequest()
        						.webApiGet(ssnProUserResURL, token)
        						.getBody()
        						.jsonPath(),
        					noteBookName, emrName);

        if (!gettingStatus.contains("terminated"))
            throw new Exception("Computational resources has not been terminated for Notebook " + noteBookName);
        System.out.println("   Computational resources has been terminated for Notebook " + noteBookName);

        Amazon.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrName, AmazonInstanceState.TERMINATED);
        Docker.checkDockerStatus(nodePrefix + "_stop_exploratory_NotebookAutoTest", publicIp);

        
        System.out.println("10. Notebook will be started ...");
        String myJs = "{\"notebook_instance_name\":\"" + noteBookName + "\"}";
        Response respStartNotebook = new HttpRequest().webApiPost(ssnExpEnvURL, ContentType.JSON,
                                                                  myJs, token);
        System.out.println("    respStartNotebook.getBody() is " + respStartNotebook.getBody().asString());
        Assert.assertEquals(respStartNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "starting", PropertyValue.getTimeoutNotebookStartup());
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook " + noteBookName + " has not been started");
        System.out.println("    Notebook " + noteBookName + " has been started");

        Amazon.checkAmazonStatus(amazonNodePrefix + "-nb-" + noteBookName, AmazonInstanceState.RUNNING);
        Docker.checkDockerStatus(nodePrefix + "_start_exploratory_NotebookAutoTest", publicIp);

        
        System.out.println("11. New EMR will be deployed for termination ...");
        final String emrNewName = "New" + emrName; 
        deployEMR.setEmr_instance_count("2");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrNewName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRNew = new HttpRequest().webApiPut(ssnCompResURL,
                                                                       ContentType.JSON, deployEMR, token);
        System.out.println("    responseDeployingEMRNew.getBody() is " + responseDeployingEMRNew.getBody().asString());
        Assert.assertEquals(responseDeployingEMRNew.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName, "creating", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("configuring"))
            throw new Exception("New EMR " + emrNewName + " has not been deployed");
        System.out.println("    New EMR " + emrNewName + " has been deployed");
        
        System.out.println("   Waiting until EMR has been configured ...");
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName, "configuring", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR " + emrNewName + " has not been configured");
        System.out.println("   EMR " + emrNewName + " has been configured");

        Amazon.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName, AmazonInstanceState.RUNNING);
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);

        System.out.println("    New EMR will be terminated ...");
        final String ssnTerminateEMRURL = getSnnURL(Path.getTerminateEMRUrl(noteBookName, emrNewName));
        System.out.println("    SSN terminate EMR URL is " + ssnTerminateEMRURL);
        
        Response respTerminateEMR = new HttpRequest().webApiDelete(ssnTerminateEMRURL,
                                                                   ContentType.JSON, token);
        System.out.println("    respTerminateEMR.getBody() is " + respTerminateEMR.getBody().asString());
        Assert.assertEquals(respTerminateEMR.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName, "terminating", PropertyValue.getTimeoutEMRTerminate());
        if (!gettingStatus.contains("terminated"))
            throw new Exception("New EMR " + emrNewName + " has not been terminated");
        System.out.println("    New EMR " + emrNewName + " has been terminated");

        Amazon.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName, AmazonInstanceState.TERMINATED);
        Docker.checkDockerStatus(nodePrefix + "_terminate_computational_NewEMRAutoTest", publicIp);

        
        System.out.println("12. New EMR will be deployed for notebook termination ...");
        final String emrNewName2 = "AnotherNew" + emrName;
        
        System.out.println("    SSN terminate EMR URL is " + ssnTerminateEMRURL);
		System.out.println("    New EMR will be deployed ...");
        deployEMR.setEmr_instance_count("2");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrNewName2);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRAnotherNew = new HttpRequest().webApiPut(ssnCompResURL,
                                                                              ContentType.JSON, deployEMR,
                                                                              token);
        System.out.println("    responseDeployingEMRAnotherNew.getBody() is " + responseDeployingEMRAnotherNew.getBody().asString());
        Assert.assertEquals(responseDeployingEMRAnotherNew.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName2, "creating", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("configuring"))
            throw new Exception("New emr " + emrNewName2 + " has not been deployed");
        System.out.println("    New emr " + emrNewName2 + " has been deployed");
        
        System.out.println("   Waiting until EMR has been configured ...");
        gettingStatus = waitWhileEmrStatus(ssnProUserResURL, token, noteBookName, emrNewName2, "configuring", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR " + emrNewName2 + " has not been configured");
        System.out.println("   EMR " + emrNewName2 + " has been configured");

        Amazon.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName2, AmazonInstanceState.RUNNING);
        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);

        
        System.out.println("13. Notebook will be terminated ...");
        final String ssnTerminateNotebookURL = getSnnURL(Path.getTerminateNotebookUrl(noteBookName));
        Response respTerminateNotebook = new HttpRequest().webApiDelete(ssnTerminateNotebookURL, ContentType.JSON, token);
        System.out.println("    respTerminateNotebook.getBody() is " + respTerminateNotebook.getBody().asString());
        Assert.assertEquals(respTerminateNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileNotebookStatus(ssnProUserResURL, token, noteBookName, "terminating", PropertyValue.getTimeoutEMRTerminate());
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
        System.out.println("    EMR has been terminated for Notebook " + noteBookName);

        Amazon.checkAmazonStatus(amazonNodePrefix + "-nb-NotebookAutoTest", AmazonInstanceState.TERMINATED);
        Amazon.checkAmazonStatus(amazonNodePrefix + "-emr-" + noteBookName + "-" + emrNewName2, AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus(nodePrefix + "_terminate_exploratory_NotebookAutoTestt", publicIp);
    }
    
    private String getBucketName() {
    	return String.format("%s-%s-bucket", serviceBaseName, PropertyValue.getUsernameSimple()).replace('_', '-').toLowerCase();
    }
    
    private String getEmrClusterName(String emrName) throws Exception {
        Instance instance = Amazon.getInstance(emrName);
        for (Tag tag : instance.getTags()) {
			if (tag.getKey().equals("Name")) {
		        return tag.getValue();
			}
		}
        throw new Exception("Could not detect cluster name for EMR " + emrName);
    }

    private void copyFileToSSN(String filename, String ip) throws IOException, InterruptedException {
        String sourceDir = "/var/lib/jenkins/AutoTestData";
        String copyToSSNCommand = "scp -i %s -o 'StrictHostKeyChecking no' %s ubuntu@%s:~/";

        System.out.println(String.format("Copying %s...", filename));
        String command = String.format(copyToSSNCommand,
        		PropertyValue.getAccessKeyPrivFileName(),
                Paths.get(sourceDir, filename).toString(),
                ip);
        AckStatus status = HelperMethods.executeCommand(command);
        System.out.println(String.format("Copied %s: %s", filename, status.toString()));
        Assert.assertTrue(status.isOk());
    }
    
    private void copyFileToNotebook(Session session, String filename, String ip) throws JSchException, IOException, InterruptedException {
    	String copyToNotebookCommand = "scp -i %s -o 'StrictHostKeyChecking no' ~/%s ubuntu@%s:/tmp/";
    	String command = String.format(copyToNotebookCommand,
    			PropertyValue.getAccessKeyPrivFileNameSSN(),
                filename,
                ip);
        
    	System.out.println(String.format("Copying %s...", filename));
    	System.out.println(String.format("  Run command: %s", command));
        ChannelExec copyResult = SSHConnect.setCommand(session, command);
        AckStatus status = SSHConnect.checkAck(copyResult);
        System.out.println(String.format("Copied %s: %s", filename, status.toString()));
        Assert.assertTrue(status.isOk());
    }
    
    private void testPython(String ssnIP, String noteBookIp, String emrName, String cluster_name)
            throws JSchException, IOException, InterruptedException {
    	String [] files = {
    			"kernels_test.py",
    			"train.csv",
    			"PYTHON.ipynb",
    			"R.ipynb",
    			"SCALA.ipynb"
    			};
    	String pyFilename = files[0];

        System.out.println("Copying files to SSN...");
        for (String filename : files) {
            copyFileToSSN(filename, ssnIP);
		}
        
        System.out.println(String.format("Copying files to Notebook %s...", noteBookIp));
        Session ssnSession = SSHConnect.getConnect("ubuntu", ssnIP, 22);

        String command;
        AckStatus status;
        try {
            for (String filename : files) {
            	copyFileToNotebook(ssnSession, filename, noteBookIp);
    		}

            System.out.println(String.format("Port forwarding from ssn " + ssnIP + " to notebook %s...", noteBookIp));
            int assignedPort = ssnSession.setPortForwardingL(0, noteBookIp, 22);
            System.out.println(String.format("Port forwarded localhost:%s -> %s:22", assignedPort, noteBookIp));

            Session notebookSession = SSHConnect.getForwardedConnect("ubuntu", noteBookIp, assignedPort);

            try {
                command = String.format("/usr/bin/python %s --region %s --bucket %s --cluster_name %s",
                        Paths.get("/tmp", pyFilename).toString(),
                        PropertyValue.getAwsRegion(),
                        getBucketName(),
                        cluster_name);
                System.out.println(String.format("Executing command %s...", command));
                ChannelExec runScript = SSHConnect.setCommand(notebookSession, command);
                status = SSHConnect.checkAck(runScript);
                Assert.assertTrue(status.isOk(), "The python script works not correct");

                System.out.println("Python script was work correct ");
            }
            finally {
                notebookSession.disconnect();
            }
        }
        finally {
            ssnSession.disconnect();
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
            System.out.println("ERROR: Timeout has been expired for SSN available.");
            System.out.println("  timeout is " + duration);
            return false;
        } else {
    		System.out.println("Current status code for SSN is " + actualStatus);
    	}
        
        return true;
    }

    private int waitWhileStatus(String url, String token, int status, Duration duration)
            throws InterruptedException {
    	System.out.println("Wait for status code " + status + " with URL " + url + " with token " + token);
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
            System.out.println("ERROR: Timeout has been expired for request.");
            System.out.println("  URL is " + url);
            System.out.println("  token is " + token);
            System.out.println("  status is " + status);
            System.out.println("  timeout is " + duration);
    	} else {
    		System.out.println("Current status code for " + url + " is " + actualStatus);
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
    	System.out.println("Wait for status " + status + " with URL " + url + " with token " + token + " for notebook " + notebookName);
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
            System.out.println("ERROR: Timeout has been expired for request.");
            System.out.println("  URL is " + url);
            System.out.println("  token is " + token);
            System.out.println("  status is " + status);
            System.out.println("  timeout is " + duration);
        } else {
        	System.out.println("Current state for Notebook " + notebookName + " is " + actualStatus);
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
    	System.out.println("Wait for status " + status + " with URL " + url + " with token " + token + " for computational " +
            computationalName + " on notebook " + notebookName);
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
            System.out.println("ERROR: Timeout has been expired for request.");
            System.out.println("  URL is " + url);
            System.out.println("  token is " + token);
            System.out.println("  status is " + status);
            System.out.println("  timeout is " + duration);
        } else {
        	System.out.println("Current state for EMR " + computationalName + " on notebook " + notebookName + " is " + actualStatus);
        }
        
        return actualStatus;
    }
    
}
