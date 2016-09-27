package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class FolderListener extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWarmuper.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    private FileHandler fileHandler;

    public void start(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
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
        Path directory = Paths.get(configuration.getResponseDirectory());
        WatchService watcher = directory.getFileSystem().newWatchService();
        directory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        WatchKey watckKey = watcher.poll(configuration.getPollTimeout(), TimeUnit.SECONDS);
        if (watckKey != null) {
            List<WatchEvent<?>> events = watckKey.pollEvents();
            for (WatchEvent event : events) {
                fileHandler.handle(event.context().toString());
                pollFile();
            }
        }
    }
}
