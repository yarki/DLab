package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.dto.ResourceBaseDTO;
import com.epam.dlab.generate_json.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Vladyslav_Valt on 10/21/2016.
 */
@Singleton
public class CommandBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandBuilder.class);

    public String buildCommand(RunDockerCommand runDockerCommand, ResourceBaseDTO resourceBaseDTO) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder("echo -e '");
        try {
            sb.append(new JsonGenerator().generateJson(resourceBaseDTO));
        } catch (JsonProcessingException e) {
            LOGGER.error("ERROR generating json from dockerRunParameters: " + e.getMessage());
            throw e;
        }
        sb.append('\'');
        sb.append(" | ");
        sb.append(runDockerCommand.toCMD());

        return sb.toString();
    }
}
