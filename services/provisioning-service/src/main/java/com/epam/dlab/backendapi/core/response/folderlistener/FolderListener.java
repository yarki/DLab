/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.backendapi.core.response.folderlistener;

import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.exceptions.DlabException;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FolderListener implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderListener.class);

    private final String directory;
    private final Duration timeout;
    private final FileHandlerCallback fileHandlerCallback;
    private final Duration fileLengthCheckDelay;
    private volatile boolean success;

    public FolderListener(String directory, Duration timeout, FileHandlerCallback fileHandlerCallback, Duration fileLengthCheckDelay) {
        this.directory = directory;
        this.timeout = timeout;
        this.fileHandlerCallback = fileHandlerCallback;
        this.fileLengthCheckDelay = fileLengthCheckDelay;
    }

    @Override
    public void run() {
        pollFile();
    }

    private void pollFile() {
        Path directoryPath = Paths.get(directory);
        boolean responsed = false;
        try (WatchService watcher = directoryPath.getFileSystem().newWatchService()) {
            directoryPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            LOGGER.debug("Registered a new watcher for directory {}", directoryPath.toAbsolutePath().toString());
            while (true) {
                final WatchKey watchKey = watcher.poll(timeout.toSeconds(), TimeUnit.SECONDS);
                if (watchKey != null) {
                    for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                        final WatchEvent.Kind<?> kind = watchEvent.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            String fileName = watchEvent.context().toString();
                            boolean victim = fileHandlerCallback.checkUUID(DockerCommands.extractUUID(fileName));
                            LOGGER.debug("Caught {} response file creation, skipping: {}", fileName, !victim);
                            if (victim) {
                                handleFileAsync(fileName);
                                responsed = true;
                                break;
                            }
                        }
                    }
                    boolean valid = watchKey.reset();
                    if (!valid || responsed) {
                        break;
                    }
                } else if (!success) {
                    LOGGER.debug("Either could not receive a response, or there was an error during response processing");
                    fileHandlerCallback.handleError();
                    break;
                }
            }
        } catch (Exception e) {
            throw new DlabException("FolderListenerExecutor exception", e);
        }
    }

    private void handleFileAsync(String fileName) {
        CompletableFuture
                .supplyAsync(new AsyncFileHandler(fileName, directory, fileHandlerCallback, fileLengthCheckDelay))
                .thenAccept(result -> success = success || result);
    }
}
