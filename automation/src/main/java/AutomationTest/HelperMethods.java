package AutomationTest;

import DockerHelper.AckStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HelperMethods {

    public static String generateRandomValue(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddmmss");
        return "AutoTest" + simpleDateFormat.format(new Date());
    }

    public static AckStatus executeCommand(String command) throws IOException, InterruptedException {
        System.out.println(String.format("Executing command: %s", command));
        Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
        int status = process.waitFor();
        String message = "";
        if(status != 0) {
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                    output.append(System.lineSeparator());
                }
            }
            message = output.toString();
        }
        return new AckStatus(status, message);
    }
    
}
