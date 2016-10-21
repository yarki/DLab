package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.core.docker.command.ImagesDockerCommand;
import com.epam.dlab.backendapi.core.docker.command.UnixCommand;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Created by Alexey Suprun
 */
public interface DockerCommands {
    String GET_IMAGES = new ImagesDockerCommand()
            .pipe(UnixCommand.awk("{print $1\":\"$2}"))
            .pipe(UnixCommand.sort())
            .pipe(UnixCommand.uniq())
            .pipe(UnixCommand.grep("dlab"))
            .pipe(UnixCommand.grep("none", "-v"))
            .pipe(UnixCommand.grep("base", "-v"))
            .pipe(UnixCommand.grep("ssn", "-v"))
            .pipe(UnixCommand.grep("edge", "-v"))
            .toCMD();

    ObjectMapper MAPPER = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

    static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    static String extractUUID(String fileName) {
        return fileName.replace(Constants.JSON_EXTENSION, "");
    }
}
