package com.epam.dlab.backendapi.core.docker.command;

/**
 * Created by Maksym_Pendyshchuk on 10/24/2016.
 */
public enum DockerAction {
    DESCRIBE,
    CREATE,
    START,
    RUN,
    STOP,
    TERMINATE;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}
