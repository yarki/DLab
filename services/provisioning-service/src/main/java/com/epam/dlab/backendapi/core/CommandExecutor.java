package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.dto.EMRBaseDTO;
import com.epam.dlab.generate_json.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class CommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    public List<String> executeSync(String command) throws IOException {
        Process process = execute(command);
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }

    public void executeAsync(final String command) {
        new Thread(() -> execute(command)).start();
    }

    private Process execute(String command) {
        Process process = null;
        try {
            LOGGER.debug("Execute command: {}", command);
            process = Runtime.getRuntime().exec(createCommand(command));
            process.waitFor();

        } catch (Exception e) {
            LOGGER.error("execute command:", e);
        }
        return process;
    }

    private String[] createCommand(String command) {
        return new String[]{"bash", "-c", command};
    }
}
