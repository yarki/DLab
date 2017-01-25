package AutomationTest;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Tag;
import com.jayway.restassured.RestAssured;
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

    private String serviceBaseName;
    private String ssnURL;
    private String publicIp;

    private final static Logger logger = Logger.getLogger(TestServices.class.getName());


    @BeforeClass
    public static void Setup() throws InterruptedException {
        // loading log4j.xml file
        DOMConfigurator.configure("log4j.xml");

        // Load properties
        PropertyValue.getJenkinsJobURL();
    }
    
    @AfterClass
    public static void Cleanup() throws InterruptedException {
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
        JenkinsCall jenkins = new JenkinsCall(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword());
        String buildNumber = jenkins.getLastJenkinsJob(PropertyValue.getJenkinsJobURL());
        System.out.println("   Jenkins Job found:");
        
        ssnURL = jenkins.getSsnURL().replaceAll(" ", "");
        serviceBaseName = jenkins.getServiceBaseName().replaceAll(" ", "");
        Assert.assertNotNull(ssnURL, "Jenkins URL was not generated");
        Assert.assertNotNull(serviceBaseName, "Service BaseName was not generated");
        System.out.println("Build number is: " + buildNumber);
        System.out.println("JenkinsURL is: " + ssnURL);
        System.out.println("ServiceBaseName is: " + serviceBaseName);

        System.out.println("Check status of SSN node on Amazon:");
        DescribeInstancesResult describeInstanceResult = Amazon.getInstanceResult(serviceBaseName + "-ssn");
        InstanceState instanceState = describeInstanceResult
        	.getReservations()
        	.get(0)
        	.getInstances()
        	.get(0)
            .getState();
        publicIp = describeInstanceResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
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
        Amazon.checkAmazonStatus(serviceBaseName + "-" + nodePrefix + "-edge", AmazonInstanceState.RUNNING);

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

        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "status", "creating", PropertyValue.getTimeoutNotebookCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook " + noteBookName + " has not been created");
        System.out.println("   Notebook " + noteBookName + " has been created");

        Amazon.checkAmazonStatus(nodePrefix + "-nb-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus(nodePrefix + "_create_exploratory_NotebookAutoTest", publicIp);
        
        //get notebook IP
        DescribeInstancesResult describeInstanceResult = Amazon.getInstanceResult(noteBookName);
        String notebookIp = describeInstanceResult.getReservations()
        		.get(0)
        		.getInstances()
        		.get(0)
        		.getPrivateIpAddress();
        
/*
        System.out.println("8. EMR will be deployed ...");
        final String ssnCompResURL = getSnnURL(Path.COMPUTATIONAL_RES);
        System.out.println("   SSN computational resources URL is " + ssnCompResURL);
        
        DeployEMRDto deployEMR = new DeployEMRDto();
        final String emrVersion="emr-5.2.0";
        deployEMR.setEmr_instance_count("3");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMR = new HttpRequest().webApiPut(ssnCompResURL, ContentType.JSON,
                                                                    deployEMR, token);
        System.out.println("   responseDeployingEMR.getBody() is " + responseDeployingEMR.getBody().asString());
        Assert.assertEquals(responseDeployingEMR.statusCode(), HttpStatusCode.OK);

        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "computational_resources.status", "creating", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("EMR " + emrName + " has not been deployed");
        System.out.println("   EMR " + emrName + " has been deployed");

        Amazon.checkAmazonStatus(nodePrefix + "-emr-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus(nodePrefix + "_create_computational_EMRAutoTest", publicIp);
        
        //run python script
        testPython(publicIp, notebookIp, serviceBaseName, emrName, getEmrClusterName(emrName));
*/
        System.out.println("9. Notebook will be stopped ...");
        final String ssnStopNotebookURL = getSnnURL(Path.getStopNotebookUrl(noteBookName));
        System.out.println("   SSN stop notebook URL is " + ssnStopNotebookURL);

        Response responseStopNotebook = new HttpRequest().webApiDelete(ssnStopNotebookURL,
                                                                       ContentType.JSON, token);
        System.out.println("   responseStopNotebook.getBody() is " + responseStopNotebook.getBody().asString());
        Assert.assertEquals(responseStopNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "status", "stopping", PropertyValue.getTimeoutNotebookShutdown());
        if (!gettingStatus.contains("stopped"))
            throw new Exception("Notebook " + noteBookName + " has not been stopped");
        System.out.println("   Notebook " + noteBookName + " has been stopped");
        gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath().getString("computational_resources.status");
        if (!gettingStatus.contains("terminated"))
            throw new Exception("Computational resources has not been terminated for Notebook " + noteBookName);
        System.out.println("   Computational resources has been terminated for Notebook " + noteBookName);

        Amazon.checkAmazonStatus(nodePrefix + "-emr-" + noteBookName, AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus(nodePrefix + "_stop_exploratory_NotebookAutoTest", publicIp);

        System.out.println("10. Notebook will be started ...");
        String myJs = "{\"notebook_instance_name\":\"" + noteBookName + "\"}";
        Response respStartNotebook = new HttpRequest().webApiPost(ssnExpEnvURL, ContentType.JSON,
                                                                  myJs, token);
        System.out.println("    respStartNotebook.getBody() is " + respStartNotebook.getBody().asString());
        Assert.assertEquals(respStartNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "status", "starting", PropertyValue.getTimeoutNotebookStartup());
        if (!gettingStatus.contains("running"))
            throw new Exception("Notebook " + noteBookName + " has not been started");
        System.out.println("    Notebook " + noteBookName + " has been started");

        Amazon.checkAmazonStatus(nodePrefix + "-nb-" + noteBookName, AmazonInstanceState.RUNNING);

        Docker.checkDockerStatus(nodePrefix + "_start_exploratory_NotebookAutoTest", publicIp);
/*
        System.out.println("11. New EMR will be deployed for termination ...");
        final String emrNewName = "New" + emrName; 
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version(emrVersion);
        deployEMR.setName(emrNewName);
        deployEMR.setNotebook_name(noteBookName);
        Response responseDeployingEMRNew = new HttpRequest().webApiPut(ssnCompResURL,
                                                                       ContentType.JSON, deployEMR, token);
        System.out.println("    responseDeployingEMRNew.getBody() is " + responseDeployingEMRNew.getBody().asString());
        Assert.assertEquals(responseDeployingEMRNew.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "computational_resources.status", "creating", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("New EMR " + emrNewName + " has not been deployed");
        System.out.println("    New EMR " + emrNewName + " has been deployed");

        System.out.println("    New EMR will be terminated ...");
        final String ssnTerminateEMRURL = getSnnURL(Path.getTerminateEMRUrl(noteBookName, emrNewName));
        System.out.println("    SSN terminate EMR URL is " + ssnTerminateEMRURL);
        
        Response respTerminateEMR = new HttpRequest().webApiDelete(ssnTerminateEMRURL,
                                                                   ContentType.JSON, token);
        System.out.println("    respTerminateEMR.getBody() is " + respTerminateEMR.getBody().asString());
        Assert.assertEquals(respTerminateEMR.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "computational_resources.status", "terminating", PropertyValue.getTimeoutEMRTerminate());
        if (!gettingStatus.contains("terminated"))
            throw new Exception("New EMR " + emrNewName + " has not been terminated");
        System.out.println("    New EMR " + emrNewName + " has been terminated");

        Amazon.checkAmazonStatus(emrNewName, AmazonInstanceState.TERMINATED);

        Docker.checkDockerStatus(nodePrefix + "_terminate_computational_NewEMRAutoTest", publicIp);

        System.out.println("12. New EMR will be deployed for notebook termination ...");
        final String emrNewName2 = "AnotherNew" + emrName;
        
        System.out.println("    SSN terminate EMR URL is " + ssnTerminateEMRURL);
		System.out.println("    New EMR will be deployed ...");
        deployEMR.setEmr_instance_count("1");
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
        
        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "computational_resources.status", "creating", PropertyValue.getTimeoutEMRCreate());
        if (!gettingStatus.contains("running"))
            throw new Exception("New emr " + emrNewName2 + " has not been deployed");
        System.out.println("    New emr " + emrNewName2 + " has been deployed");
*/
        System.out.println("13. Notebook will be terminated ...");
        final String ssnTerminateNotebookURL = getSnnURL(Path.getTerminateNotebookUrl(noteBookName));
        Response respTerminateNotebook = new HttpRequest().webApiDelete(ssnTerminateNotebookURL, ContentType.JSON, token);
        System.out.println("    respTerminateNotebook.getBody() is " + respTerminateNotebook.getBody().asString());
        Assert.assertEquals(respTerminateNotebook.statusCode(), HttpStatusCode.OK);
        
        gettingStatus = waitWhileStatus(ssnProUserResURL, token, "status", "terminating",
        		PropertyValue.getTimeoutNotebookTerminate() + PropertyValue.getTimeoutEMRTerminate());
        if (!gettingStatus.contains("terminated"))
            throw new Exception("Notebook" + noteBookName + " has not been terminated");
        gettingStatus = new HttpRequest().webApiGet(ssnProUserResURL, token).getBody().jsonPath()
            .getString("computational_resources.status");
        if (!gettingStatus.contains("terminated"))
            throw new Exception("EMR has been terminated for Notebook " + noteBookName);
        System.out.println("    EMR has been terminated for Notebook " + noteBookName);

        Amazon.checkAmazonStatus(nodePrefix + "-nb-NotebookAutoTest", AmazonInstanceState.TERMINATED);
/*
        Amazon.checkAmazonStatus(emrNewName2, AmazonInstanceState.TERMINATED);
*/

        Docker.checkDockerStatus(nodePrefix + "_terminate_exploratory_NotebookAutoTestt", publicIp);
    }
    
    private static String getEmrClusterName(String emrName) throws Exception {
        Instance instance = Amazon.getInstanceResult(emrName)
        		.getReservations()
        		.get(0)
        		.getInstances()
        		.get(0);
        for (Tag tag : instance.getTags()) {
			if (tag.getKey().equals("Name")) {
		        return tag.getValue();
			}
		}
        throw new Exception("Could not detect cluster name for EMR " + emrName);
    }

    private static void copyFileToSSN(String filename, String ip) throws IOException, InterruptedException {
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
    
    private static void copyFileToNotebook(Session session, String filename, String ip) throws JSchException, IOException, InterruptedException {
    	String copyToNotebookCommand = "scp -i %s -o 'StrictHostKeyChecking no' ~/%s ubuntu@%s:/tmp/";
    	String command = String.format(copyToNotebookCommand,
    			PropertyValue.getAccessKeyPrivFileNameSSN(),
                filename,
                ip);
        
        System.out.println(String.format("Copying %s...", filename));
        ChannelExec copyResult = SSHConnect.setCommand(session, command);
        AckStatus status = SSHConnect.checkAck(copyResult);
        System.out.println(String.format("Copied %s: %s", filename, status.toString()));
        Assert.assertTrue(status.isOk());
    }
    
    private static void testPython(String ssnIP, String noteBookIp, String serviceBaseName, String emrName, String cluster_name)
            throws JSchException, IOException, InterruptedException {
    	final String nodePrefix = PropertyValue.getUsernameSimple();

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

            System.out.println(String.format("Port forwarding to notebook %s...", noteBookIp));
            int assignedPort = ssnSession.setPortForwardingL(0, noteBookIp, 22);
            System.out.println(String.format("Port forwarded localhost:%s -> %s:22", assignedPort, noteBookIp));

            Session notebookSession = SSHConnect.getForwardedConnect("ubuntu", noteBookIp, assignedPort);

            try {
                String bucketName = String.format("%s-%s-bucket", serviceBaseName, nodePrefix).replace('_', '-').toLowerCase();
                command = String.format("/usr/bin/python %s --bucket %s --cluster_name %s",
                        Paths.get("/tmp", pyFilename).toString(),
                        bucketName,
                        cluster_name);
                System.out.println(String.format("Executing command %s...", command));
                ChannelExec runScript = SSHConnect.setCommand(notebookSession, command);
                status = SSHConnect.checkAck(runScript);
                System.out.println(String.format("Executed command: %s", status.toString()));
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

    private boolean waitForSSNService(int timeout) throws InterruptedException {
        HttpRequest request = new HttpRequest();
        int actualStatus;
        long expiredTime = System.currentTimeMillis() + timeout * 1000;

        while ((actualStatus = request.webApiGet(ssnURL, ContentType.TEXT).statusCode()) != HttpStatusCode.OK) {
            Thread.sleep(1000);
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                actualStatus = request.webApiGet(ssnURL, ContentType.TEXT).statusCode();
                break;
            }
        }

        if (actualStatus != HttpStatusCode.OK) {
            System.out.println("ERROR: Timeout has been expired for SSN available.");
            System.out.println("  timeout is " + 0);
            return false;
        }
        return true;
    }

    private static int waitWhileStatus(String url, String token, int status, int timeout)
            throws InterruptedException {
        HttpRequest request = new HttpRequest();
        int actualStatus;
        long expiredTime = System.currentTimeMillis() + timeout * 1000;

        while ((actualStatus = request.webApiGet(url, token).getStatusCode()) == status) {
            Thread.sleep(1000);
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                actualStatus = request.webApiGet(url, token).getStatusCode();
                break;
            }
        }

        if (actualStatus == status) {
            System.out.println("ERROR: Timeout has been expired for request.");
            System.out.println("  URL is " + url);
            System.out.println("  token is " + token);
            System.out.println("  status is " + status);
            System.out.println("  timeout is " + timeout);
        }
        return actualStatus;
    }

    private static String waitWhileStatus(String url, String token, String statusPath, String status, int timeout)
            throws InterruptedException {
        HttpRequest request = new HttpRequest();
        String actualStatus;
        long expiredTime = System.currentTimeMillis() + timeout * 1000;

        while ((actualStatus = request.webApiGet(url, token).getBody().jsonPath().getString(statusPath)).contains(status)) {
            Thread.sleep(1000);
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                actualStatus = request.webApiGet(url, token).getBody().jsonPath().getString(statusPath);
                break;
            }
        }

        if (actualStatus.contains(status)) {
            System.out.println("ERROR: Timeout has been expired for request.");
            System.out.println("  URL is " + url);
            System.out.println("  token is " + token);
            System.out.println("  statusPath is " + statusPath);
            System.out.println("  status is " + status);
            System.out.println("  timeout is " + timeout);
        }
        return actualStatus;
    }

}
