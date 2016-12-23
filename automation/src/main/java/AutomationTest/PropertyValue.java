package AutomationTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyValue {
    
    String result = "";
    InputStream inputStream;

    
    public String getPropValues(String propName) throws IOException {
       
        try {
                
                File f1 = new File("/var/lib/jenkins/AutoTestData/config.properties");
                FileReader fin = new FileReader(f1);
                Properties pr = new Properties();
                pr.load(fin);
                               
                result = pr.getProperty(propName);
                
                  String JENKINS_USERNANE=pr.getProperty("JENKINS_USERNANE");
                  String  JENKINS_PASSWORD=pr.getProperty("JENKINS_PASSWORD");
                  String  USERNANE=pr.getProperty("USERNANE");
                  String  PASSWORD=pr.getProperty("PASSWORD");
                  String  NOT_IAM_USERNAME=pr.getProperty("NOT_IAM_USERNAME");
                  String  NOT_IAM_PASSWORD=pr.getProperty("NOT_IAM_PASSWORD");
                  String  NOT_DLAB_USERNAME=pr.getProperty("NOT_DLAB_USERNAME");
                  String  NOT_DLAB_PASSWORD=pr.getProperty("NOT_DLAB_PASSWORD");
                  String JENKINS_JOB_URL=pr.getProperty("JENKINS_JOB_URL");
                  String  USER_FOR_ACTIVATE_KEY=pr.getProperty("USER_FOR_ACTIVATE_KEY");
                  String  PASSWORD_FOR_ACTIVATE_KEY=pr.getProperty("PASSWORD_FOR_ACTIVATE_KEY") ;
                  
                  System.out.println("JENKINS_USERNANE is " + JENKINS_USERNANE);
                  System.out.println("JENKINS_PASSWORD is " + JENKINS_PASSWORD);
                  System.out.println("USERNANE is " + USERNANE);
                  System.out.println("PASSWORD is " + PASSWORD);
                  System.out.println("NOT_IAM_USERNAME is " + NOT_IAM_USERNAME);
                  System.out.println("NOT_IAM_PASSWORD is " + NOT_IAM_PASSWORD);
                  System.out.println("NOT_DLAB_USERNAME is " + NOT_DLAB_USERNAME);
                  System.out.println("NOT_DLAB_PASSWORD is " + NOT_DLAB_PASSWORD);
                  System.out.println("JENKINS_JOB_URL is " + JENKINS_JOB_URL);
                  System.out.println("USER_FOR_ACTIVATE_KEY is " + USER_FOR_ACTIVATE_KEY);
                  System.out.println("PASSWORD_FOR_ACTIVATE_KEY is " + PASSWORD_FOR_ACTIVATE_KEY);
                 
                
                if (result == null) throw new Exception ("Value from property file is equal null");
                 
        
        } catch (Exception e) {
                System.out.println("Exception: " + e);
        }
        return result;
    }
}


