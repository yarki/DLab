package com.epam.dlab.backendapi.core.response;

/**
 * Created by Alexey Suprun
 */
public interface FileHandler {
    boolean handle(String fileName, byte[] content) throws Exception;
}
