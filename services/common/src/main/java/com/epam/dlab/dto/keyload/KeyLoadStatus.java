package com.epam.dlab.dto.keyload;

import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * Created by Alexey Suprun
 */
public enum KeyLoadStatus {
    NONE("none", null, Response.Status.NOT_FOUND),
    NEW("new", null, Response.Status.NOT_ACCEPTABLE),
    SUCCESS("success", "ok", Response.Status.OK),
    ERROR("error", "err", Response.Status.OK);

    private String status;
    private String value;
    private Response.Status httpStatus;

    KeyLoadStatus(String status, String value, Response.Status httpStatus) {
        this.status = status;
        this.value = value;
        this.httpStatus = httpStatus;
    }

    public String getStatus() {
        return status;
    }

    public Response.Status getHttpStatus() {
        return httpStatus;
    }

    public static boolean isSuccess(String value) {
        return SUCCESS.value.equals(value);
    }

    public static String getStatus(boolean successed) {
        return successed ? SUCCESS.status : ERROR.status;
    }

    public static KeyLoadStatus findByStatus(String status) {
        return Arrays.stream(values()).reduce(NONE, (result, next) -> next.status.equals(status) ? next : result);
    }
}
