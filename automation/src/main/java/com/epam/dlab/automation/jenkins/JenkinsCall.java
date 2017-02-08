package com.epam.dlab.automation.jenkins;

import com.epam.dlab.automation.helper.TestNamingHelper;
import com.epam.dlab.automation.helper.PropertyValue;
import com.epam.dlab.automation.http.HttpStatusCode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.given;

public class JenkinsCall {
    private final static Logger LOGGER = LogManager.getLogger(JenkinsCall.class);
	private static final long JENKINS_REQUEST_TIMEOUT = 5000;
    
    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    
    private String ssnURL;
    private String serviceBaseName;
    
    FormAuthConfig config = new FormAuthConfig("/Auto_tests/search/", "username", "password");
    
    public JenkinsCall(){
        awsAccessKeyId = convertToParam(PropertyValue.getAwsAccessKeyId());
        awsSecretAccessKey = convertToParam(PropertyValue.getAwsSecretAccessKey());
    }
    
    private String convertToParam(String s) {
    	s= s.replaceAll("/", "%2F");
    	return s;
    }
    
    public String getSsnURL() {
        return ssnURL;
    }

    public String getServiceBaseName() {
        return serviceBaseName;
    }
    
    private String getQueueStatus() {
    	return given().header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==")
    			.auth().form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config)
                .contentType(ContentType.XML).when()
                .get("api/xml").getBody()
                .xmlPath().getString("freeStyleProject.inQueue");
    }

    private boolean waitForJenkinsStartup(Duration duration) throws InterruptedException {
    	String actualStatus;
    	long timeout = duration.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;
        
    	while ((actualStatus = getQueueStatus()).endsWith("true")) {
            Thread.sleep(JENKINS_REQUEST_TIMEOUT);
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
            	actualStatus = getQueueStatus();
            	break;
            }
        }
        
        if (actualStatus.endsWith("true")) {
            LOGGER.info("ERROR: Timeout has been expired for Jenkins");
            LOGGER.info("  timeout is {}");
            return false;
        }
        return true;
    }

    public String runJenkinsJob(String jenkinsJobURL) throws Exception {
        RestAssured.baseURI = jenkinsJobURL;
        String dateAsString = TestNamingHelper.generateRandomValue();
        Response responsePostJob = given().header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==")
        		.auth().form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config)
        		.contentType("application/x-www-form-urlencoded")
        		.body("name=Access_Key_ID&value=" + awsAccessKeyId +
        				"&name=Secret_Access_Key&value=" + awsSecretAccessKey +
        				"&name=Infrastructure_Tag&value=" + dateAsString +
        				"name=OS_user&value=ubuntu&name=Cloud_provider&value=aws&name=OS_family&value=debian&name=Action&value=create" +
        				"&json=%7B%22parameter"+
        				"%22%3A+%5B%7B%22name%22%3A+%22Access_Key_ID%22%2C+%22value%22%3A+%22" + awsAccessKeyId +
        				"%22%7D%2C+%7B%22name%22%3A+%22Secret_Access_Key%22%2C+%22value%22%3A+%22"+ awsSecretAccessKey +
        				"%22%7D%2C+%7B%22name%22%3A+%22Infrastructure_Tag%22%2C+%22value%22%3A+%22" + dateAsString +
        				"%22%7D%2C+%7B%22name%22%3A+%22OS_user%22%2C+%22value%22%3A+%22ubuntu" +
        				"%22%7D%2C+%7B%22name%22%3A+%22Cloud_provider%22%2C+%22value%22%3A+%22aws" +
        				"%22%7D%2C+%7B%22name%22%3A+%22OS_family%22%2C+%22value%22%3A+%22debian" +
        				"%22%7D%2C+%7B%22name%22%3A+%22Action%22%2C+%22value%22%3A+%22create" +
        				"%22%7D%5D%7D&Submit=Build")
        		.when()
        		.post(jenkinsJobURL + "build");
        Assert.assertEquals(responsePostJob.statusCode(), HttpStatusCode.OK);
        
        waitForJenkinsStartup(PropertyValue.getTimeoutJenkinsAutotest());
        
        setBuildNumber();
        checkBuildResult();
        setJenkinsURLServiceBaseName();
        
        return PropertyValue.getJenkinsBuildNumber();
    }

    public String getJenkinsJob() throws Exception {
        RestAssured.baseURI = PropertyValue.getJenkinsJobURL();

        setBuildNumber();
        checkBuildResult();
        setJenkinsURLServiceBaseName();

        return PropertyValue.getJenkinsBuildNumber();
    }

    private void setBuildNumber() throws Exception {
        if (PropertyValue.getJenkinsBuildNumber() != null) {
            LOGGER.info("Jenkins build number is {}", PropertyValue.getJenkinsBuildNumber());
        	return;
    	}
        
        String buildName = given()
                .header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==")
                .auth().form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config)
                .contentType("application/x-www-form-urlencoded").when()
                .get("lastBuild").getBody().htmlPath().getString("html.head.title");
        
        Pattern pattern = Pattern.compile("\\s#\\d+(?!\\d+)\\s");      
        Matcher matcher = pattern.matcher(buildName);
        if(matcher.find()) {
        	PropertyValue.setJenkinsBuildNumber(matcher.group().substring(2).trim());         
        } else {
        	throw new Exception("Jenkins job was failed. There is no buildNumber");
        }
        LOGGER.info("Jenkins build number is {}", PropertyValue.getJenkinsBuildNumber());
    }
    
    private void checkBuildResult() throws Exception {
    	String buildResult;
    	long timeout = PropertyValue.getTimeoutJenkinsAutotest().toMillis();
    	long expiredTime = System.currentTimeMillis() + timeout;
        
        do {
        	buildResult = given().header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==")
        			.auth().form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config)
        			.contentType(ContentType.JSON).when()
        			.get(PropertyValue.getJenkinsBuildNumber() + "/api/json?pretty=true")
        			.getBody().jsonPath().getString("result");
            if (buildResult == null) {
            	if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
            		throw new Exception("Timeout has been expired for Jenkins build. Timeout is " + PropertyValue.getTimeoutJenkinsAutotest());
            	}
            	Thread.sleep(JENKINS_REQUEST_TIMEOUT);
            }
        } while (buildResult == null);
        
        if(!buildResult.equals("SUCCESS")) {
        	throw new Exception("Jenkins job was failed. Build result is not success");
        }
    }

    private void setJenkinsURLServiceBaseName() throws Exception {
        String jenkinsHoleURL = given().header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==")
        		.auth().form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config)
        		.contentType(ContentType.TEXT).when()
        		.get(PropertyValue.getJenkinsBuildNumber() + "/logText/progressiveText?start=0")
        		.getBody().prettyPrint();
        Pattern pattern = Pattern.compile("Jenkins URL:(.+)");      
        Matcher matcher = pattern.matcher(jenkinsHoleURL);
        if(matcher.find()) {
        	ssnURL = matcher.group(1).replaceAll("/jenkins", "");         
        }
            
        pattern = Pattern.compile("Service base name:(.+)");      
        matcher = pattern.matcher(jenkinsHoleURL);
        if(matcher.find()) {
        	serviceBaseName = matcher.group(1);         
        } else {
        	throw new Exception("SSN URL in Jenkins job not found");
        }
    }  
}
