package com.epam.dlab.dto.keyload;

/**
 * Created by Alexey Suprun
 */
public enum KeyLoadStatus {
    NEW("new", null),
    SUCCESS("success", "ok"),
    ERROR("error", "err");

    private String status;
    private String value;

    KeyLoadStatus(String status, String value) {
        this.status = status;
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public static boolean isSuccess(String value) {
        return SUCCESS.value.equals(value);
    }

    public static String getStatus(boolean successed) {
        return successed ? SUCCESS.status : ERROR.status;
    }
}
