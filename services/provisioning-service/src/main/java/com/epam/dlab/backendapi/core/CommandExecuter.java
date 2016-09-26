package com.epam.dlab.backendapi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexey Suprun
 */
public class CommandExecuter {
    public static List<String> execute(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(createCommand(command));
        process.waitFor();
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }

    private static String[] createCommand(String command) {
        return new String[]{"/bin/sh", "-c", command};
    }
}
