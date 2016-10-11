package com.epam.dlab.backendapi.core.response.folderlistener;

import com.epam.dlab.backendapi.core.response.FileHandler;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Alexey Suprun
 */
public class AsyncFileHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderListener.class);

    private String fileName;
    private String directory;
    private FileHandler fileHandler;
    private Duration fileLengthCheckDelay;

    public AsyncFileHandler(String fileName, String directory, FileHandler fileHandler, Duration fileLengthCheckDelay) {
        this.fileName = fileName;
        this.directory = directory;
        this.fileHandler = fileHandler;
        this.fileLengthCheckDelay = fileLengthCheckDelay;
    }

    @Override
    public void run() {
        Path path = Paths.get(directory, fileName);
        try {
            if (fileHandler.handle(fileName, readBytes(path))) {
                Files.delete(path);
            }
        } catch (Exception e) {
            LOGGER.debug("handle file async", e);
        }
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
