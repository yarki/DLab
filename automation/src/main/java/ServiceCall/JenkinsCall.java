package ServiceCall;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import AutomationTest.*;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.testng.Assert;

import static com.jayway.restassured.RestAssured.given;

public class JenkinsCall {
    
    private String jenkinsUserName;
    private String jenkinsPassword;
    private String buildNumber;
    private String ssnURL;
    private String serviceBaseName;
    private String buildResult;
    
    FormAuthConfig config = new FormAuthConfig("/Auto_tests/search/", "username", "password");
    
    public JenkinsCall(String  jenkinsUserName, String jenkinsPassword){
        this.jenkinsUserName = jenkinsUserName;
        this.jenkinsPassword = jenkinsPassword;
    }
    
    public JenkinsCall(){
        
    }
    
    public String getSsnURL() {
        return ssnURL;
    }

    public String getServiceBaseName() {
        return serviceBaseName;
    }
    
    private String getQueueStatus() {
    	return given().header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==").auth()
                .form(jenkinsUserName, jenkinsPassword, config).
                contentType(ContentType.XML).
                when(). 
                get("api/xml").getBody().xmlPath().getString("freeStyleProject.inQueue");
    }

    private boolean waitForJenkinsStartup(Duration duration) throws InterruptedException {
    	String actualStatus;
    	long timeout = duration.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;
        
    	while ((actualStatus = getQueueStatus()).endsWith("true")) {
            Thread.sleep(1000);
            if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
            	actualStatus = getQueueStatus();
            	break;
            }
        };
        
        if (actualStatus.endsWith("true")) {
            System.out.println("ERROR: Timeout has been expired for Jenkins");
            System.out.println("  timeout is " + 0);
            return false;
        }
        return true;
    }

    public String runJenkinsJob(String jenkinsJobURL) throws Exception {
        RestAssured.baseURI = jenkinsJobURL;
        String dateAsString = HelperMethods.generateRandomValue();
        Response responsePostJob = given().
            header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==").auth().form(jenkinsUserName, jenkinsPassword, config).
            contentType("application/x-www-form-urlencoded").
            body("name=Access_Key_ID&value=AKIAIBQUQDBWTN3PJXIA&name=Secret_Access_Key&value=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&name=Infrastructure_Tag&value=" + dateAsString + "&name=Action&value=create&statusCode=303&redirectTo=.&Jenkins-Crumb=20ae9bcd0457a068e9d2488cfc997cbe&json=%7B%22parameter%22%3A+%5B%7B%22name%22%3A+%22Access_Key_ID%22%2C+%22value%22%3A+%22AKIAIPLDO42GUJGUG55Q%22%7D%2C+%7B%22name%22%3A+%22Secret_Access_Key%22%2C+%22value%22%3A+%228eGcOKL3e1I8s%2FDpoAgM5wqTtffX7CQQrWigLYHD%22%7D%2C+%7B%22name%22%3A+%22Infrastructure_Tag%22%2C+%22value%22%3A+%22" + dateAsString+ "%22%7D%2C+%7B%22name%22%3A+%22Action%22%2C+%22value%22%3A+%22create%22%7D%5D%2C+%22statusCode%22%3A+%22303%22%2C+%22redirectTo%22%3A+%22.%22%2C+%22Jenkins-Crumb%22%3A+%2220ae9bcd0457a068e9d2488cfc997cbe%22%7D&Submit=Build").
            when(). 
            post(jenkinsJobURL + "build");
        Assert.assertEquals(responsePostJob.statusCode(), 200);
        
        //wait until build is not in queue
        waitForJenkinsStartup(PropertyValue.getTimeoutJenkinsAutotest());
        
        getBuildNumber();
        getBuildResult();
        setJenkinsURLServiceBaseName();
        
        return buildNumber;
    }

    public String getLastJenkinsJob(String jenkinsJobURL) throws Exception {
        RestAssured.baseURI = jenkinsJobURL;

        getBuildNumber();
        getBuildResult();
        setJenkinsURLServiceBaseName();

        return buildNumber;
    }

    public String getBuildNumber() throws Exception {
        String buildName = given()
                .header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==")
                .auth()
                .form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config)
                .contentType("application/x-www-form-urlencoded")
                .when()
                .get("lastBuild").getBody().htmlPath().getString("html.head.title");
        
        Pattern pattern = Pattern.compile("\\s#\\d+(?!\\d+)\\s");      
        Matcher matcher = pattern.matcher(buildName);
        if(matcher.find()) {
            buildNumber = matcher.group().substring(2).trim();         
        }
        else throw new Exception("Jenkins job was failed. There is no buildNumber");
        System.out.println("Jenkins build number is " + buildNumber);
        return buildNumber;
    }
    
    public String getBuildResult() throws Exception {
        //wait until job return build result     
        do{
            buildResult = given().header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==").auth().form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config).
                contentType(ContentType.JSON).
                when(). 
                get(buildNumber + "/api/json?pretty=true").getBody().jsonPath().getString("result");
        }while(buildResult == null);
        return buildResult;
    }

    public void setJenkinsURLServiceBaseName() throws Exception {
           
        String jenkinsHoleURL;
        if(buildResult.equals("SUCCESS")){

            jenkinsHoleURL = given().header("Authorization", "Basic YWRtaW46Vmxlc3VSYWRpbGFzRWxrYQ==").auth().form(PropertyValue.getJenkinsUsername(), PropertyValue.getJenkinsPassword(), config).
            contentType(ContentType.TEXT).
            when(). 
            get(buildNumber + "/logText/progressiveText?start=0").getBody().prettyPrint();
                      
            Pattern pattern1 = Pattern.compile("Jenkins URL:(.+)");      
            Matcher matcher1 = pattern1.matcher(jenkinsHoleURL);
            if(matcher1.find()) {
                ssnURL = matcher1.group(1).replaceAll("/jenkins", "");         
            }
            
            Pattern pattern2 = Pattern.compile("Service base name:(.+)");      
            Matcher matcher2 = pattern2.matcher(jenkinsHoleURL);
            if(matcher2.find()) {
                serviceBaseName = matcher2.group(1);         
            }
        }
        else throw new Exception("Jenkins job was failed. Build result is not success");
        
    }  
}
