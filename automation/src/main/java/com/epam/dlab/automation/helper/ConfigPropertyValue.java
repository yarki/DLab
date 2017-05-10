package com.epam.dlab.automation.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.util.Properties;

public class ConfigPropertyValue {

    private final static Logger LOGGER = LogManager.getLogger(ConfigPropertyValue.class);
    public static final String CONFIG_FILE_NAME;


    public static final String JENKINS_USERNAME="JENKINS_USERNAME";
    public static final String JENKINS_PASSWORD="JENKINS_PASSWORD";
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
    private static final String ACCESS_KEY_PRIV_FILE_NAME_SSN="ACCESS_KEY_PRIV_FILE_NAME_SSN";
    
    private static final String AWS_ACCESS_KEY_ID="AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_ACCESS_KEY="AWS_SECRET_ACCESS_KEY";
    private static final String AWS_REGION="AWS_REGION";
    private static final String AWS_REQUEST_TIMEOUT="AWS_REQUEST_TIMEOUT";
    
	public static final String TIMEOUT_JENKINS_AUTOTEST="TIMEOUT_JENKINS_AUTOTEST";
	public static final String TIMEOUT_UPLOAD_KEY="TIMEOUT_UPLOAD_KEY";
	public static final String TIMEOUT_NOTEBOOK_CREATE="TIMEOUT_NOTEBOOK_CREATE";
	public static final String TIMEOUT_NOTEBOOK_STARTUP="TIMEOUT_NOTEBOOK_STARTUP";
	public static final String TIMEOUT_NOTEBOOK_SHUTDOWN="TIMEOUT_NOTEBOOK_SHUTDOWN";
	public static final String TIMEOUT_NOTEBOOK_TERMINATE="TIMEOUT_NOTEBOOK_TERMINATE";
	public static final String TIMEOUT_EMR_CREATE="TIMEOUT_EMR_CREATE";
	public static final String TIMEOUT_EMR_TERMINATE="TIMEOUT_EMR_TERMINATE";

    public static final String JUPYTER_SCENARIO_FILES ="JUPYTER_SCENARIO_FILES";

    public static String jenkinsBuildNumber;
    public static String CLUSTER_OS_USERNAME;
    public static String CLUSTER_OS_FAMILY;


    private static final Properties props = new Properties();

    public static final String CLUSTER_USER = "cluster.username";

    public static final String CLUSTER_OS = "cluster.os.family";

    static {
        CONFIG_FILE_NAME = PropertiesResolver.getConfFileLocation();
        jenkinsBuildNumber = System.getProperty("jenkins.buildNumber", "");
        if(StringUtils.isEmpty(jenkinsBuildNumber)) {
            LOGGER.error(new Exception("Missed required parameter 'jenkins.buildNumber'"));
            System.exit(0);
        }

    	loadProperties();

        CLUSTER_OS_USERNAME  = System.getProperty(CLUSTER_USER, "");
        if(StringUtils.isEmpty(CLUSTER_OS_USERNAME)) {
            LOGGER.error(new Exception("Missed required parameter " + CLUSTER_USER));
            System.exit(0);
        }
        CLUSTER_OS_FAMILY  = System.getProperty(CLUSTER_OS, "");
        if(StringUtils.isEmpty(CLUSTER_OS_FAMILY)) {
            LOGGER.error(new Exception("Missed required parameter " + CLUSTER_OS));
            System.exit(0);
        }
    }
    
    private ConfigPropertyValue() { }
	
    private static Duration getDuration(String duaration) {
    	return Duration.parse("PT" + duaration);
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
        LOGGER.info("{} is {}", propertyName , props.getProperty(propertyName));
	}
	
	private static void loadProperties() {
        try {
            File f1 = new File(CONFIG_FILE_NAME);
            FileReader fin = new FileReader(f1);
            props.load(fin);

            if (StringUtils.isNotEmpty(System.getProperty(CLUSTER_USER))) {
                props.setProperty(CLUSTER_USER, System.getProperty(CLUSTER_USER));
            }
            if (StringUtils.isNotEmpty(System.getProperty(CLUSTER_OS))) {
                props.setProperty(CLUSTER_OS, System.getProperty(CLUSTER_OS));
            }
            if (StringUtils.isNotEmpty(System.getProperty(AWS_REGION))) {
                props.setProperty(AWS_REGION, System.getProperty(AWS_REGION));
            }


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
        printProperty(ACCESS_KEY_PRIV_FILE_NAME_SSN);
        
        printProperty(TIMEOUT_JENKINS_AUTOTEST);
        printProperty(TIMEOUT_UPLOAD_KEY);
        printProperty(TIMEOUT_NOTEBOOK_CREATE);
        printProperty(TIMEOUT_NOTEBOOK_STARTUP);
        printProperty(TIMEOUT_NOTEBOOK_SHUTDOWN);
        printProperty(TIMEOUT_NOTEBOOK_TERMINATE);
        printProperty(TIMEOUT_EMR_CREATE);
        printProperty(TIMEOUT_EMR_TERMINATE);

        printProperty(JUPYTER_SCENARIO_FILES);
        printProperty(CLUSTER_USER);
        printProperty(CLUSTER_OS);
    }
    
    
    public static String getJenkinsBuildNumber() {
    	return jenkinsBuildNumber;
    }

    public static void setJenkinsBuildNumber(String jenkinsBuildNumber) {
    	ConfigPropertyValue.jenkinsBuildNumber = jenkinsBuildNumber;
    }

    public static String getJenkinsUsername() {
    	return get(JENKINS_USERNAME);
    }
    
    public static String getJenkinsPassword() {
    	return get(JENKINS_PASSWORD);
    }

    public static String getUsername() {
    	return get(USERNAME);
    }
    
    public static String getUsernameSimple() {
    	String s = get(USERNAME);
		int i = s.indexOf('@');
		return (i == -1 ? s : s.substring(0, i).toLowerCase());
	}

    public static String getPassword() {
    	return get(PASSWORD);
    }

    public static String getNotIAMUsername() {
    	return get(NOT_IAM_USERNAME);
    }

    public static String getNotIAMPassword() {
    	return get(NOT_IAM_PASSWORD);
    }

    public static String getNotDLabUsername() {
    	return get(NOT_DLAB_USERNAME);
    }

    public static String getNotDLabPassword() {
    	return get(NOT_DLAB_PASSWORD);
    }

    public static String getJenkinsJobURL() {
    	return get(JENKINS_JOB_URL);
    }

    public static String getUserForActivateKey() {
    	return get(USER_FOR_ACTIVATE_KEY);
    }

    public static String getPasswordForActivateKey() {
    	return get(PASSWORD_FOR_ACTIVATE_KEY);
    }

    public static String getAccessKeyPrivFileName() {
    	File file = new File(get(ACCESS_KEY_PRIV_FILE_NAME));
        return file.getAbsolutePath();
    }

    public static String getAccessKeyPubFileName() {
    	File file = new File(get(ACCESS_KEY_PUB_FILE_NAME));
        return file.getAbsolutePath();
    }

    public static String getAccessKeyPrivFileNameSSN() {
        File file = new File(get(ACCESS_KEY_PRIV_FILE_NAME_SSN));
        return file.getAbsolutePath();
    }

    public static String getAwsAccessKeyId() {
        return get(AWS_ACCESS_KEY_ID);
    }

    public static String getAwsSecretAccessKey() {
        return get(AWS_SECRET_ACCESS_KEY);
    }

	public static String getAwsRegion() {
	    return get(AWS_REGION, System.getProperty(AWS_REGION));
	}

	public static Duration getAwsRequestTimeout() {
    	return getDuration(get(AWS_REQUEST_TIMEOUT, "10s"));
    }

	
    public static Duration getTimeoutJenkinsAutotest() {
    	return getDuration(get(TIMEOUT_JENKINS_AUTOTEST, "0s"));
    }

    public static Duration getTimeoutUploadKey() {
    	return getDuration(get(TIMEOUT_UPLOAD_KEY, "0s"));
    }

    public static Duration getTimeoutNotebookCreate() {
    	return getDuration(get(TIMEOUT_NOTEBOOK_CREATE, "0s"));
    }

    public static Duration getTimeoutNotebookStartup() {
    	return getDuration(get(TIMEOUT_NOTEBOOK_STARTUP, "0s"));
    }

    public static Duration getTimeoutNotebookShutdown() {
    	return getDuration(get(TIMEOUT_NOTEBOOK_SHUTDOWN, "0s"));
    }

    public static int getTimeoutNotebookTerminate() {
    	return get(TIMEOUT_NOTEBOOK_TERMINATE, 0);
    }

    public static Duration  getTimeoutEMRCreate() {
    	return getDuration(get(TIMEOUT_EMR_CREATE, "0s"));
    }

    public static Duration getTimeoutEMRTerminate() {
    	return getDuration(get(TIMEOUT_EMR_TERMINATE, "0s"));
    }

}
