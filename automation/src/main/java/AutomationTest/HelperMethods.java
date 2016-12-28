package AutomationTest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HelperMethods {

    public static String generateRandomValue(){
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddmmss");       
        return "AutoTest" + simpleDateFormat.format(new Date());
        
    }
    
}
