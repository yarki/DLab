package com.epam.dlab.backendapi.core.response.folderlistener;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.backendapi.core.response.folderlistener.WatchItem.ItemStatus;

import io.dropwizard.util.Duration;

public class WatchItemList implements DockerCommands {
	private static final Logger LOGGER = LoggerFactory.getLogger(WatchItemList.class);
	
	private final String directoryName;
    private final Path directoryPath;
    private final String directoryFullName;
	
	private final Vector<WatchItem> list = new Vector<WatchItem>();
	
	private String uuidSearch;
	
	private final FileHandlerCallback handlerSearch = new FileHandlerCallback() {
		public String getUUID() {
			return uuidSearch;
		}
		
		public boolean checkUUID(String uuid) {
			return uuidSearch.equals(uuid);
		}

		public void handleError() { }
		
		public boolean handle(String fileName, byte[] content) throws Exception {
			return false;
		}
		
	};

	public WatchItemList(String directoryName) {
		this.directoryName = directoryName;
        this.directoryPath = Paths.get(directoryName);
        this.directoryFullName = directoryPath.toAbsolutePath().toString();
	}
	
	
	public String getDirectoryName() {
		return directoryName;
	}

	public Path getDirectoryPath() {
		return directoryPath;
	}

	public String getDirectoryFullName() {
		return directoryFullName;
	}
	
	
	public void append(FileHandlerCallback fileHandlerCallback, long timeoutMillis, long fileLengthCheckDelay, boolean fileExists) {
	    if ( fileExists ) {
	    	//See WatchItem(..., boolean fileExists)
	    	throw new RuntimeException("Not implemented");
	    }
		WatchItem item = new WatchItem(fileHandlerCallback, timeoutMillis, fileLengthCheckDelay, fileExists);
		int index = Collections.binarySearch(list, item);
		if (index < 0) {
			index = -index;
			if (index > list.size()) {
				list.add(item);
			} else {
				list.add(index - 1, item);
			}
		} else {
			list.get(index).setExpiredTimeMillis(timeoutMillis);
		}
	}

	public void remove(int index) {
		list.remove(index);
	}
	
	public int size() {
		return list.size();
	}

	public WatchItem get(int index) {
		return list.get(index);
	}
	
	public int getIndex(String uid) {
		uuidSearch = uid;
		return Collections.binarySearch(list, new WatchItem(handlerSearch, 0, 0));
	}
	
	public WatchItem getItem(String fileName) {
		String uuid = DockerCommands.extractUUID(fileName);
		int index = getIndex(uuid);
		if ( index < 0 ) {
			return null;
		}
		return get(index);
	}
	
	private void runAsync(WatchItem item) {
		LOGGER.debug("Process file {} for folder {}", item.getFileName(), directoryFullName);
		item.setFuture(CompletableFuture.supplyAsync(
				new AsyncFileHandler(item.getFileName(), getDirectoryName(),
					item.getFileHandlerCallback(), Duration.milliseconds(item.getFileLengthCheckDelay()))));
	}
	
	public boolean processItem(WatchItem item) {
		if ( item.getStatus() == ItemStatus.FILE_CAPTURED ) {
			runAsync(item);
			return true;
		}
		
		if ( item.isExpired() ) {
			LOGGER.warn("Watch time has expired for UUID {} in folder {}", item.getFileHandlerCallback().getUUID(), directoryFullName);
		}
		return false;
	}

	public int processItemAll() {
		int count = 0;
		for (int i = 0; i < size(); i++) {
			WatchItem item = get(i);
			if ( item.getStatus() == ItemStatus.FILE_CAPTURED ) {
				if ( processItem(item) ) {
					count++;
				}
			}
		}
		if ( count > 0 ) {
			LOGGER.debug("Runs processing {} files for folder {}", count, directoryName);
		}
		return count;
	}

}
