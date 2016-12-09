package com.epam.dlab.backendapi.core;

import java.io.IOException;
import java.util.List;

/**
 * Created by dev on 30.11.16.
 */
public interface ICommandExecutor {
    List<String> executeSync(String command) throws IOException, InterruptedException;
    void executeAsync(String command);
}
