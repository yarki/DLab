package com.epam.dlab.backendapi.api.instance;

/**
 * Created by Alexey Suprun
 */
public enum UserInstanceStatus {
    CREATING("creating"),
    RUNNING("running"),
    STOPPING("stopping"),
    STOPPED("stopped"),
    TERMINATING("terminating"),
    TERMINATED("terminated");

    private String status;

    UserInstanceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
