package com.epam.dlab.automation.docker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHolder {

    private static final PropertiesHolder INSTANCE = new PropertiesHolder();

       private  Properties properties = new Properties();

       private PropertiesHolder() {
               properties = load();
       }

       public static PropertiesHolder getInstance() {
               return INSTANCE;
       }

       public String getProperty(String key) {
           return properties.getProperty(key.toLowerCase());
       }

       private Properties load() {
                      
                    InputStream input = null;

                    try {
                            //TODO: figure out what is docker.properties, where it is located.
                            input = new FileInputStream("docker.properties");
                            properties.load(input);

                    } catch (IOException ex) {
                            ex.printStackTrace();
                    } finally {
                            if (input != null) {
                                    try {
                                            input.close();
                                    } catch (IOException e) {
                                            e.printStackTrace();
                                    }
                            }
                    }
            
                    return properties;
       }
    
    
}
