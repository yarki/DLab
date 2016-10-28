package com.epam.dlab.backendapi.core.response.folderlistener;

/**
 * Created by Alexey Suprun
 */
public interface FileHandlerCallback {
    boolean checkUUID(String uuid);
    boolean handle(String fileName, byte[] content) throws Exception;
    void handleError();
}
