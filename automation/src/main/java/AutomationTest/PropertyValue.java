package AutomationTest;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class PropertyValue {
	
	public static final String CONFIG_FILE_NAME="/var/lib/jenkins/AutoTestData/config.properties";
//	public static final String CONFIG_FILE_NAME="config.properties";
	
    public static final String JENKINS_USERNANE="JENKINS_USERNANE";
    public static final String JENKINS_PASSWORD="JENKINS_PASSWORD";
    public static final String USERNANE="USERNANE";
    public static final String PASSWORD="PASSWORD";
    public static final String NOT_IAM_USERNAME="NOT_IAM_USERNAME";
    public static final String NOT_IAM_PASSWORD="NOT_IAM_PASSWORD";
    public static final String NOT_DLAB_USERNAME="NOT_DLAB_USERNAME";
    public static final String NOT_DLAB_PASSWORD="NOT_DLAB_PASSWORD";
    public static final String JENKINS_JOB_URL="JENKINS_JOB_URL";
    public static final String USER_FOR_ACTIVATE_KEY="USER_FOR_ACTIVATE_KEY";
    public static final String PASSWORD_FOR_ACTIVATE_KEY="PASSWORD_FOR_ACTIVATE_KEY";
    
    public static final String TEST_BEFORE_SLEEP_SECONDS="TEST_BEFORE_SLEEP_SECONDS";
    public static final String TEST_AFTER_SLEEP_SECONDS="TEST_AFTER_SLEEP_SECONDS";

    private static final Properties props = new Properties();
    
    static {
    	loadProperties();
    }
	
	public static String get(String propertyName) {
		return get(propertyName, "");
	}

	public static String get(String propertyName, String defaultValue) {
		return props.getProperty(propertyName, defaultValue);
	}
	
	public static int get(String propertyName, int defaultValue) {
		if (props == null) {
			loadProperties();
		}
		String s = props.getProperty(propertyName, String.valueOf(defaultValue)); 
		return Integer.parseInt(s);
	}
	
	private static void printProperty(String propertyName) {
		System.out.println(propertyName + " is " + props.getProperty(propertyName));
	}
	
    public static void loadProperties() {
        try {
                File f1 = new File(CONFIG_FILE_NAME);
                FileReader fin = new FileReader(f1);
                props.load(fin);
        } catch (Exception e) {
                throw new RuntimeException("Load properties from \"" + CONFIG_FILE_NAME + "\" fail", e);
        }
        
        printProperty(JENKINS_USERNANE);
        printProperty(JENKINS_PASSWORD);
        printProperty(USERNANE);
        printProperty(PASSWORD);
        printProperty(NOT_IAM_USERNAME);
        printProperty(NOT_IAM_PASSWORD);
        printProperty(NOT_DLAB_USERNAME);
        printProperty(NOT_DLAB_PASSWORD);
        printProperty(JENKINS_JOB_URL);
        printProperty(USER_FOR_ACTIVATE_KEY);
        printProperty(PASSWORD_FOR_ACTIVATE_KEY);
        
        printProperty(TEST_BEFORE_SLEEP_SECONDS);
        printProperty(TEST_AFTER_SLEEP_SECONDS);
    }
}


