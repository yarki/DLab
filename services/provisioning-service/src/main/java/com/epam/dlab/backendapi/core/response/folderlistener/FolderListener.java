package com.epam.dlab.backendapi.core.response.folderlistener;

import com.epam.dlab.backendapi.core.Constants;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.FileHandler;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexey Suprun
 */
public class FolderListener implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderListener.class);

    private String directory;
    private Duration timeout;
    private FileHandler fileHandler;
    private Duration fileLengthCheckDelay;

    public FolderListener(String directory, Duration timeout, FileHandler fileHandler, Duration fileLengthCheckDelay) {
        this.directory = directory;
        this.timeout = timeout;
        this.fileHandler = fileHandler;
        this.fileLengthCheckDelay = fileLengthCheckDelay;
    }

    @Override
    public void run() {
        try {
            pollFile();
        } catch (Exception e) {
            LOGGER.error("FolderListenerExecutor exception", e);
        }
    }

    private void pollFile() throws Exception {
        Path directoryPath = Paths.get(directory);
        WatchService watcher = directoryPath.getFileSystem().newWatchService();
        directoryPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        WatchKey watchKey = watcher.poll(timeout.toSeconds(), TimeUnit.SECONDS);
        if (watchKey != null) {
            List<WatchEvent<?>> events = watchKey.pollEvents();
            for (WatchEvent event : events) {
                String fileName = event.context().toString();
                if (fileName.endsWith(Constants.JSON_EXTENSION)) {
                    handleFileAsync(fileName);
                }
                pollFile();
            }
        }
    }

    private void handleFileAsync(String fileName) {
        CompletableFuture.runAsync(new AsyncFileHandler(fileName, directory, fileHandler, fileLengthCheckDelay));
    }
}
