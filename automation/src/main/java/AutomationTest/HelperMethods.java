package AutomationTest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HelperMethods {

    public static String generateRandomValue(){
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddmmss");       
        return "AutoTest" + simpleDateFormat.format(new Date());
        
    }
    
    public static String getFilePath(String path) {
                
        File file = new File("/AutoTestData/" + path);
        return file.getAbsolutePath();
        
    }
}
