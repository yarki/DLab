package AutomationTest;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class PropertyValue {
	
	public static final String CONFIG_FILE_NAME="/var/lib/jenkins/AutoTestData/config.properties";
//	private static final String CONFIG_FILE_NAME="config.properties";
	
	private static final String JENKINS_USERNAME="JENKINS_USERNAME";
	private static final String JENKINS_PASSWORD="JENKINS_PASSWORD";
	private static final String USERNAME="USERNAME";
	private static final String PASSWORD="PASSWORD";
	private static final String NOT_IAM_USERNAME="NOT_IAM_USERNAME";
	private static final String NOT_IAM_PASSWORD="NOT_IAM_PASSWORD";
	private static final String NOT_DLAB_USERNAME="NOT_DLAB_USERNAME";
	private static final String NOT_DLAB_PASSWORD="NOT_DLAB_PASSWORD";
	private static final String JENKINS_JOB_URL="JENKINS_JOB_URL";
	private static final String USER_FOR_ACTIVATE_KEY="USER_FOR_ACTIVATE_KEY";
	private static final String PASSWORD_FOR_ACTIVATE_KEY="PASSWORD_FOR_ACTIVATE_KEY";
	private static final String ACCESS_KEY_PRIV_FILE_NAME="ACCESS_KEY_PRIV_FILE_NAME";
	private static final String ACCESS_KEY_PUB_FILE_NAME="ACCESS_KEY_PUB_FILE_NAME";
    
	public static final String TEST_BEFORE_SLEEP_SECONDS="TEST_BEFORE_SLEEP_SECONDS";
	public static final String TEST_AFTER_SLEEP_SECONDS="TEST_AFTER_SLEEP_SECONDS";
	
	public static final String TIMEOUT_JENKINS_AUTOTEST="TIMEOUT_JENKINS_AUTOTEST";
	public static final String TIMEOUT_SSN_CREATE="TIMEOUT_SSN_CREATE";
	public static final String TIMEOUT_UPLOAD_KEY="TIMEOUT_UPLOAD_KEY";
	public static final String TIMEOUT_NOTEBOOK_CREATE="TIMEOUT_NOTEBOOK_CREATE";
	public static final String TIMEOUT_NOTEBOOK_STARTUP="TIMEOUT_NOTEBOOK_STARTUP";
	public static final String TIMEOUT_NOTEBOOK_SHUTDOWN="TIMEOUT_NOTEBOOK_SHUTDOWN";
	public static final String TIMEOUT_NOTEBOOK_TERMINATE="TIMEOUT_NOTEBOOK_TERMINATE";
	public static final String TIMEOUT_EMR_CREATE="TIMEOUT_EMR_CREATE";
	public static final String TIMEOUT_EMR_TERMINATE="TIMEOUT_EMR_TERMINATE";
	

    private static final Properties props = new Properties();
    
    static {
    	loadProperties();
    }
    
    
    private PropertyValue() { }
    
	
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
	
	private static void loadProperties() {
        try {
                File f1 = new File(CONFIG_FILE_NAME);
                FileReader fin = new FileReader(f1);
                props.load(fin);
        } catch (Exception e) {
                throw new RuntimeException("Load properties from \"" + CONFIG_FILE_NAME + "\" fails. " + e.getLocalizedMessage(), e);
        }
        
        printProperty(JENKINS_USERNAME);
        printProperty(JENKINS_PASSWORD);
        printProperty(USERNAME);
        printProperty(PASSWORD);
        printProperty(NOT_IAM_USERNAME);
        printProperty(NOT_IAM_PASSWORD);
        printProperty(NOT_DLAB_USERNAME);
        printProperty(NOT_DLAB_PASSWORD);
        printProperty(JENKINS_JOB_URL);
        printProperty(USER_FOR_ACTIVATE_KEY);
        printProperty(PASSWORD_FOR_ACTIVATE_KEY);
        printProperty(ACCESS_KEY_PRIV_FILE_NAME);
        printProperty(ACCESS_KEY_PUB_FILE_NAME);
        
        printProperty(TEST_BEFORE_SLEEP_SECONDS);
        printProperty(TEST_AFTER_SLEEP_SECONDS);
        
        printProperty(TIMEOUT_JENKINS_AUTOTEST);
        printProperty(TIMEOUT_SSN_CREATE);
        printProperty(TIMEOUT_UPLOAD_KEY);
        printProperty(TIMEOUT_NOTEBOOK_CREATE);
        printProperty(TIMEOUT_NOTEBOOK_STARTUP);
        printProperty(TIMEOUT_NOTEBOOK_SHUTDOWN);
        printProperty(TIMEOUT_NOTEBOOK_TERMINATE);
        printProperty(TIMEOUT_EMR_CREATE);
        printProperty(TIMEOUT_EMR_TERMINATE);
    }
    
    
    public static final String getJenkinsUsername() {
    	return get(JENKINS_USERNAME);
    }
    
    public static final String getJenkinsPassword() {
    	return get(JENKINS_PASSWORD);
    }

    public static final String getUsername() {
    	return get(USERNAME);
    }

    public static final String getPassword() {
    	return get(PASSWORD);
    }

    public static final String getNotIAMUsername() {
    	return get(NOT_IAM_USERNAME);
    }

    public static final String getNotIAMPassword() {
    	return get(NOT_IAM_PASSWORD);
    }

    public static final String getNotDLabUsername() {
    	return get(NOT_DLAB_USERNAME);
    }

    public static final String getNotDLabPassword() {
    	return get(NOT_DLAB_PASSWORD);
    }

    public static final String getJenkinsJobURL() {
    	return get(JENKINS_JOB_URL);
    }

    public static final String getUserForActivateKey() {
    	return get(USER_FOR_ACTIVATE_KEY);
    }

    public static final String getPasswordForActivateKey() {
    	return get(PASSWORD_FOR_ACTIVATE_KEY);
    }

    public static final String getAccessKeyPrivFileName() {
    	File file = new File(get(ACCESS_KEY_PRIV_FILE_NAME));
        return file.getAbsolutePath();
    }

    public static final String getAccessKeyPubFileName() {
    	File file = new File(get(ACCESS_KEY_PUB_FILE_NAME));
        return file.getAbsolutePath();
    }



    public static final int getTimeoutJenkinsAutotest() {
    	return get(TIMEOUT_JENKINS_AUTOTEST, 0);
    }

    public static final int getTimeoutSSNCreate() {
    	return get(TIMEOUT_SSN_CREATE, 0);
    }

    public static final int getTimeoutUploadKey() {
    	return get(TIMEOUT_UPLOAD_KEY, 0);
    }

    public static final int getTimeoutNotebookCreate() {
    	return get(TIMEOUT_NOTEBOOK_CREATE, 0);
    }

    public static final int getTimeoutNotebookStartup() {
    	return get(TIMEOUT_NOTEBOOK_STARTUP, 0);
    }

    public static final int getTimeoutNotebookShutdown() {
    	return get(TIMEOUT_NOTEBOOK_SHUTDOWN, 0);
    }

    public static final int getTimeoutNotebookTerminate() {
    	return get(TIMEOUT_NOTEBOOK_TERMINATE, 0);
    }

    public static final int getTimeoutEMRCreate() {
    	return get(TIMEOUT_EMR_CREATE, 0);
    }

    public static final int getTimeoutEMRTerminate() {
    	return get(TIMEOUT_EMR_TERMINATE, 0);
    }

}


