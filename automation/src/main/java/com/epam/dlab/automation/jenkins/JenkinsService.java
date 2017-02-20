package com.epam.dlab.automation.jenkins;

import com.epam.dlab.automation.helper.ConfigPropertyValue;
import com.epam.dlab.automation.helper.TestNamingHelper;
import com.epam.dlab.automation.http.HttpStatusCode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.given;

public class JenkinsService {
    private final static Logger LOGGER = LogManager.getLogger(JenkinsService.class);

    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    
    private String ssnURL;
    private String serviceBaseName;
    
    FormAuthConfig config = new FormAuthConfig(JenkinsConfigProperties.JENKINS_JOB_NAME_SEARCH, "username", "password");
    
    public JenkinsService(){
        awsAccessKeyId = convertToParam(ConfigPropertyValue.getAwsAccessKeyId());
        awsSecretAccessKey = convertToParam(ConfigPropertyValue.getAwsSecretAccessKey());
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
    	return getWhen(ContentType.XML)
//                given()
//                .header(JenkinsConfigProperties.AUTHORIZATION, JenkinsConfigProperties.AUTHORIZATION_KEY)
//    			.auth().form(ConfigPropertyValue.getJenkinsUsername(), ConfigPropertyValue.getJenkinsPassword(), config)
//                .contentType(ContentType.XML)
//                .when()
                .get(JenkinsUrls.API).getBody()
                .xmlPath()
                .getString(JenkinsResponseElements.IN_QUEUE_ELEMENT);
    }

    private boolean waitForJenkinsStartup(Duration duration) throws InterruptedException {
    	String actualStatus;
    	long timeout = duration.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;
        
    	while ((actualStatus = getQueueStatus()).endsWith(JenkinsConfigProperties.SUCCESS_STATUS)) {
            Thread.sleep(JenkinsConfigProperties.JENKINS_REQUEST_TIMEOUT);
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
            	actualStatus = getQueueStatus();
            	break;
            }
        }
        
        if (actualStatus.endsWith(JenkinsConfigProperties.SUCCESS_STATUS)) {
            LOGGER.info("ERROR: Timeout has been expired for Jenkins");
            LOGGER.info("  timeout is {}");
            return false;
        }
        return true;
    }

    public String runJenkinsJob(String jenkinsJobURL) throws Exception {
        RestAssured.baseURI = jenkinsJobURL;
        String dateAsString = TestNamingHelper.generateRandomValue();
        Response responsePostJob = getWhen(ContentType.URLENC)
                .body(String.format(JenkinsConfigProperties.JENKINS_JOB_START_BODY,
                        awsAccessKeyId, awsSecretAccessKey, dateAsString,
                        ConfigPropertyValue.CLUSTER_OS_USERNAME, ConfigPropertyValue.CLUSTER_OS_FAMILY,
                        awsAccessKeyId, awsSecretAccessKey, dateAsString,
                        ConfigPropertyValue.CLUSTER_OS_USERNAME, ConfigPropertyValue.CLUSTER_OS_FAMILY))
//                        "name=Access_Key_ID&value=" + awsAccessKeyId +
//                        "&name=Secret_Access_Key&value=" + awsSecretAccessKey +
//                        "&name=Infrastructure_Tag&value=" + dateAsString +
//                        "name=OS_user&value="+"ubuntu" +
//                        "&name=Cloud_provider&value=aws&name=OS_family&value=" + "debian" +
//                        "&name=Action&value=create" +
//                        "&json=%7B%22parameter"+
//                        "%22%3A+%5B%7B%22name%22%3A+%22Access_Key_ID%22%2C+%22value%22%3A+%22" + awsAccessKeyId +
//                        "%22%7D%2C+%7B%22name%22%3A+%22Secret_Access_Key%22%2C+%22value%22%3A+%22"+ awsSecretAccessKey +
//                        "%22%7D%2C+%7B%22name%22%3A+%22Infrastructure_Tag%22%2C+%22value%22%3A+%22" + dateAsString +
//                        "%22%7D%2C+%7B%22name%22%3A+%22OS_user%22%2C+%22value%22%3A+%22"+"ubuntu" +
//                        "%22%7D%2C+%7B%22name%22%3A+%22Cloud_provider%22%2C+%22value%22%3A+%22aws" +
//                        "%22%7D%2C+%7B%22name%22%3A+%22OS_family%22%2C+%22value%22%3A+%22"+"debian" +
//                        "%22%7D%2C+%7B%22name%22%3A+%22Action%22%2C+%22value%22%3A+%22create" +
//                        "%22%7D%5D%7D&Submit=Build")
        		.post(jenkinsJobURL + "build");
        Assert.assertEquals(responsePostJob.statusCode(), HttpStatusCode.OK);
        
        waitForJenkinsStartup(ConfigPropertyValue.getTimeoutJenkinsAutotest());
        
        setBuildNumber();
        checkBuildResult();
        setJenkinsURLServiceBaseName();
        
        return ConfigPropertyValue.getJenkinsBuildNumber();
    }

    public String getJenkinsJob() throws Exception {
        RestAssured.baseURI = ConfigPropertyValue.getJenkinsJobURL();

        setBuildNumber();
        checkBuildResult();
        setJenkinsURLServiceBaseName();

        return ConfigPropertyValue.getJenkinsBuildNumber();
    }

    private void setBuildNumber() throws Exception {
        if (ConfigPropertyValue.getJenkinsBuildNumber() != null) {
            LOGGER.info("Jenkins build number is {}", ConfigPropertyValue.getJenkinsBuildNumber());
        	return;
    	}

        String buildName = getWhen(ContentType.URLENC)
                .get(JenkinsUrls.LAST_BUILD).getBody().htmlPath().getString(JenkinsResponseElements.HTML_TITLE);
        
        Pattern pattern = Pattern.compile("\\s#\\d+(?!\\d+)\\s");      
        Matcher matcher = pattern.matcher(buildName);
        if(matcher.find()) {
        	ConfigPropertyValue.setJenkinsBuildNumber(matcher.group().substring(2).trim());
        } else {
        	throw new Exception("Jenkins job was failed. There is no buildNumber");
        }
        LOGGER.info("Jenkins build number is {}", ConfigPropertyValue.getJenkinsBuildNumber());
    }


    private void checkBuildResult() throws Exception {
    	String buildResult;
    	long timeout = ConfigPropertyValue.getTimeoutJenkinsAutotest().toMillis();
    	long expiredTime = System.currentTimeMillis() + timeout;
        
        do {
        	buildResult = getWhen(ContentType.JSON)
        			.get(ConfigPropertyValue.getJenkinsBuildNumber() + JenkinsUrls.JSON_PRETTY)
        			.getBody()
                    .jsonPath()
                    .getString(JenkinsResponseElements.RESULT);
            if (buildResult == null) {
            	if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
            		throw new Exception("Timeout has been expired for Jenkins build. Timeout is " + ConfigPropertyValue.getTimeoutJenkinsAutotest());
            	}
            	Thread.sleep(JenkinsConfigProperties.JENKINS_REQUEST_TIMEOUT);
            }
        } while (buildResult == null);
        
        if(!buildResult.equals("SUCCESS")) {
        	throw new Exception("Jenkins job was failed. Build result is not success");
        }
    }

    private void setJenkinsURLServiceBaseName() throws Exception {
        String jenkinsHoleURL = getWhen(ContentType.TEXT)
        		.get(ConfigPropertyValue.getJenkinsBuildNumber() + JenkinsUrls.LOG_TEXT)
        		.getBody()
                .prettyPrint();
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

    private RequestSpecification getWhen(ContentType contentType) {
        return given()
                .header(JenkinsConfigProperties.AUTHORIZATION, JenkinsConfigProperties.AUTHORIZATION_KEY)
        		.auth()
                .form(ConfigPropertyValue.getJenkinsUsername(), ConfigPropertyValue.getJenkinsPassword(), config)
        		.contentType(contentType).when();
    }
}
