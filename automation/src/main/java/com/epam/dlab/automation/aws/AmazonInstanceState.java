package com.epam.dlab.automation.aws;

public enum AmazonInstanceState {
    
    RUNNING("RUNNING"), TERMINATED("TERMINATED");
    private String value;

    AmazonInstanceState(String value) {
        this.value = value;

    }

    public String value() {
        return value;
    }
}
