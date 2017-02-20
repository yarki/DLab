package com.epam.dlab.automation.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesResolver {

    private final static Logger LOGGER = LogManager.getLogger(PropertiesResolver.class);
    private static final boolean DEV_MODE;
    private static String jenkinsBuildNumber;
    public static final String CONFIG_FILE_NAME;

    //keys from application.properties(dev-application.properties)
    private static String CONF_FILE_LOCATION_PROPERTY = "conf.file.location";
    private static String KEYS_DIRECTORY_LOCATION_PROPERTY = "keys.directory.location";
    private static String PYTHON_FILES_LOCATION_PROPERTY = "python.files.location";
    private static String CLUSTER_CONFIG_FILE_LOCATION_PROPERTY = "cluster.config.file.location";

    private static Properties properties = new Properties();

    static {
        DEV_MODE = System.getProperty("run.mode", "remote").equalsIgnoreCase("dev");
        jenkinsBuildNumber = System.getProperty("jenkins.buildNumber", "");
        if (jenkinsBuildNumber.isEmpty()) {
            jenkinsBuildNumber = null;
            LOGGER.warn("Jenkins build number missed");
        }
        CONFIG_FILE_NAME = (DEV_MODE ? "dev-application.properties" : "application.properties");
        loadApplicationProperties();
    }


    private static void loadApplicationProperties() {

        InputStream input = null;

        try {

            input = PropertiesResolver.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);

            // load a properties file
            properties.load(input);

            // get the property value and print it out
            LOGGER.info(properties.getProperty(CONF_FILE_LOCATION_PROPERTY));
            LOGGER.info(properties.getProperty(KEYS_DIRECTORY_LOCATION_PROPERTY));
            LOGGER.info(properties.getProperty(PYTHON_FILES_LOCATION_PROPERTY));
            LOGGER.info(properties.getProperty(CLUSTER_CONFIG_FILE_LOCATION_PROPERTY ));

        } catch (IOException ex) {
            LOGGER.error(ex);
            LOGGER.error("Application configuration file could not be found by the path: {}", CONFIG_FILE_NAME);
            System.exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                    LOGGER.error("Application configuration file could not be found by the path: {}", CONFIG_FILE_NAME);
                }
            }



        }

    }


    public static String getConfFileLocation() {
        return properties.getProperty(CONF_FILE_LOCATION_PROPERTY);
    }

    public static String getKeysLocation() {
        return properties.getProperty(KEYS_DIRECTORY_LOCATION_PROPERTY);
    }

    public static String getPythonFilesLocation() {
        return properties.getProperty(PYTHON_FILES_LOCATION_PROPERTY);
    }

    public static String getClusterConfFileLocation() {
        return properties.getProperty(CLUSTER_CONFIG_FILE_LOCATION_PROPERTY );
    }

}
