package com.epam.dlab.backendapi.core.response;

/**
 * Created by Alexey Suprun
 */
public interface FileHandler {
    void handle(String fileName, byte[] content) throws Exception;
}
