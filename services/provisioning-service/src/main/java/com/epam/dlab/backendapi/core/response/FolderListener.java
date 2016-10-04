package com.epam.dlab.backendapi.core.response;

import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.warmup.DockerWarmuper;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexey Suprun
 */
public class FolderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWarmuper.class);

    private String directory;
    private Duration timeout;
    private FileHandler fileHandler;

    public void start(String directory, Duration timeout, FileHandler fileHandler) {
        this.directory = directory;
        this.timeout = timeout;
        this.fileHandler = fileHandler;
        new Thread(() -> {
            try {
                pollFile();
            } catch (Exception e) {
                LOGGER.error("FolderListener exception", e);
            }
        }).start();
    }

    private void pollFile() throws Exception {
        Path directoryPath = Paths.get(directory);
        WatchService watcher = directoryPath.getFileSystem().newWatchService();
        directoryPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        WatchKey watckKey = watcher.poll(timeout.toSeconds(), TimeUnit.SECONDS);
        if (watckKey != null) {
            List<WatchEvent<?>> events = watckKey.pollEvents();
            for (WatchEvent event : events) {
                String fileName = event.context().toString();
                if (fileName.endsWith(DockerCommands.JSON_EXTENTION)) {
                    fileHandler.handle(fileName, readBytes(fileName));
                }
                pollFile();
            }
        }
    }

    private byte[] readBytes(String fileName) throws IOException, InterruptedException {
        return Files.readAllBytes(Paths.get(directory, fileName));
    }
}
