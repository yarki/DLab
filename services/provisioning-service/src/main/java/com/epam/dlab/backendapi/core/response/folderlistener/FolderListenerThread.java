package com.epam.dlab.backendapi.core.response.folderlistener;

import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.FileHandler;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexey Suprun
 */
public class FolderListenerThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderListenerThread.class);

    private String directory;
    private Duration timeout;
    private FileHandler fileHandler;
    private Duration fileLengthCheckDelay;

    public FolderListenerThread(String directory, Duration timeout, FileHandler fileHandler, Duration fileLengthCheckDelay) {
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
            LOGGER.error("FolderListener exception", e);
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
                if (fileName.endsWith(DockerCommands.JSON_EXTENTION)) {
                    handleFileAsync(fileName);
                }
                pollFile();
            }
        }
    }

    private void handleFileAsync(final String fileName) {
        new Thread(() -> {
            Path path = Paths.get(directory, fileName);
            try {
                if (fileHandler.handle(fileName, readBytes(path))) {
                    Files.delete(path);
                }
            } catch (Exception e) {
                LOGGER.debug("handle file async", e);
            }
        }).start();
    }

    private byte[] readBytes(Path path) throws IOException, InterruptedException {
        File file = path.toFile();
        waitFileCompliteWrited(file, file.length());
        return Files.readAllBytes(path);
    }

    private void waitFileCompliteWrited(File file, long before) throws InterruptedException {
        Thread.sleep(fileLengthCheckDelay.toMilliseconds());
        long after = file.length();
        if (before != after) {
            waitFileCompliteWrited(file, after);
        }
    }
}
