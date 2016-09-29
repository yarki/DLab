package com.epam.dlab.backendapi.core;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecuter.class);

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
        logCommandExecute(command, result);
        return result;
    }

    private String[] createCommand(String command) {
        return new String[]{"bash", "-c", command};
    }

    private void logCommandExecute(String command, List<String> result) {
        LOGGER.debug("Execute command: {}", command);
        result.forEach(LOGGER::debug);
        LOGGER.debug("-----------------------------");
    }
}
