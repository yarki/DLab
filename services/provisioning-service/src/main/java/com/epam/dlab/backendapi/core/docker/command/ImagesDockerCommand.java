package com.epam.dlab.backendapi.core.docker.command;

import java.util.LinkedList;
import java.util.List;

public class ImagesDockerCommand implements DockerCommand {
    private String command = "docker images";

    private List<UnixCommand> unixCommands;

    public ImagesDockerCommand pipe(UnixCommand unixCommand) {
        if (unixCommands == null) {
            unixCommands = new LinkedList<>();
        }
        unixCommands.add(unixCommand);
        return this;
    }

    @Override
    public String toCMD() {
        StringBuilder sb = new StringBuilder(command);
        if (unixCommands != null) {
            for (UnixCommand unixCommand : unixCommands) {
                sb.append(" | ").append(unixCommand.getCommand());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toCMD();
    }
}
