package com.epam.dlab.backendapi.core;

import com.google.inject.Singleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class CommandExecuter {
    public List<String> execute(String command) throws IOException, InterruptedException {
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

    private String[] createCommand(String command) {
        return new String[]{"bash", "-c", command};
    }
}
