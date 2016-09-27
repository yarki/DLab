package com.epam.dlab.backendapi.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexey Suprun
 */
public class FolderListener extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWarmuper.class);

    private String responseDirectory;
    private int pollTimeout;
    private FileHandler metadataHandler;

    public FolderListener(String responseDirectory, int pollTimeout, FileHandler metadataHandler) {
        this.responseDirectory = responseDirectory;
        this.pollTimeout = pollTimeout;
        this.metadataHandler = metadataHandler;
        start();
    }

    @Override
    public void run() {
        try {
            pollFile();
        } catch (Exception e) {
            LOGGER.error("FolderListener exception", e);
        }
    }

    private void pollFile() throws Exception {
        Path directory = Paths.get(responseDirectory);
        WatchService watcher = directory.getFileSystem().newWatchService();
        directory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        WatchKey watckKey = watcher.poll(pollTimeout, TimeUnit.SECONDS);
        if (watckKey != null) {
            List<WatchEvent<?>> events = watckKey.pollEvents();
            for (WatchEvent event : events) {
                metadataHandler.handle(event.context().toString());
                pollFile();
            }
        }
    }
}
