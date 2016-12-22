package Infrastucture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import AutomationTest.Dlab.HelperMethods;
import Utils.Credential;
import Utils.Path;

import org.testng.Assert;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;

import org.testng.annotations.Test;

public class HttpRequest {

    public void AddHeader(String headerType, String headerValue)
    {
      given().header(headerType, headerValue);
    }

    public void AddAuthorizationBearer(String token)
    {
      this.AddHeader("Authorization", "Bearer " + token);
    }
    
    public Response webApiGet(String url){
        return given().contentType(ContentType.JSON).when().get(url);
    }
    
    public Response webApiGet(String url, String token){
        return given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON).when().get(url);
    }
        
    public Response webApiPost(String url, String contentType, Object body){
        return given().contentType(contentType).body(body).when().post(url);
    }      
    
    public Response webApiPost(String url, String contentType){
        return given().contentType(contentType).when().post(url);
    }
    
    public Response webApiPost(String url, String contentType, String token){
        return given().contentType(contentType).header("Authorization", "Bearer " + token).
            multiPart(new File(HelperMethods.getFilePath("user.pub"))).formParam(HelperMethods.getFilePath("user.pub")).contentType(contentType).when().post(url);
    }
    
    public Response webApiPost(String url, String contentType, Object body, String token){
        return given().contentType(contentType).header("Authorization", "Bearer " + token).body(body).when().post(url);
    }
    
    public Response webApiPut(String url, String contentType, Object body, String token){
        return given().contentType(contentType).header("Authorization", "Bearer " + token).body(body).when().put(url);
    }
    
    public Response webApiDelete(String url, String contentType, String token){
        return given().contentType(contentType).header("Authorization", "Bearer " + token).when().delete(url);
    }
    
}
