package com.epam.dlab.backendapi.core;

/**
 * Created by Alexey Suprun
 */
public interface FileHandler {
    void handle(String fileName, byte[] bytes) throws Exception;
}
