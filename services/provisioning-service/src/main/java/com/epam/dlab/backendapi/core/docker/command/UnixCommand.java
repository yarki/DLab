package com.epam.dlab.backendapi.core.docker.command;


public class UnixCommand {
    private String command;

    public UnixCommand(String command) {
        this.command = command;
    }

    public static UnixCommand awk(String txt) {
        return new UnixCommand("awk '" + txt + "'");
    }

    public static UnixCommand sort() {
        return new UnixCommand("sort");
    }

    public static UnixCommand uniq() {
        return new UnixCommand("uniq");
    }

    public static UnixCommand grep(String searchFor, String... options) {
        StringBuilder sb = new StringBuilder("grep");
        for (String option : options) {
            sb.append(' ').append(option);
        }
        sb.append(" \"" + searchFor + "\"");
        return new UnixCommand(sb.toString());
    }

    public String getCommand() {
        return command;
    }
}