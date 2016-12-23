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
                
                File f1 = new File("/AutoTestData/config.properties");
                FileReader fin = new FileReader(f1);
                Properties pr = new Properties();
                pr.load(fin);
                               
                result = pr.getProperty(propName);
                if (result == null) throw new Exception ("Value from property file is equal null");
                 
        
        } catch (Exception e) {
                System.out.println("Exception: " + e);
        }
        return result;
    }
}


