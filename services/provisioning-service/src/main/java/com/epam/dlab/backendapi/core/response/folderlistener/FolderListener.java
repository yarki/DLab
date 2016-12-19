package com.epam.dlab.backendapi.core.response.folderlistener;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.response.folderlistener.WatchItem.ItemStatus;
import com.epam.dlab.exceptions.DlabException;

public class FolderListener implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FolderListener.class);
	
	public static final long LISTENER_TIMEOUT_MILLLIS = 1000;
	
	public static final long LISTENER_IDLE_TIMEOUT_MILLLIS = 5000;

	private static final long WAIT_DIR_TIMEOUT_MILLIS = 500; 

	private static final List<FolderListener> listeners = new ArrayList<FolderListener>();


	private Thread thread;
	
	private WatchItemList itemList;
	
	private WatchService watcher;
	
	private boolean isListen = false;
	
	private long expiredIdleMillis = 0;
		
	
	private FolderListener() {	}
	
	private FolderListener(String directoryName) {
		itemList = new WatchItemList(directoryName);
	}
	

	public static WatchItem listen(String directoryName, FileHandlerCallback fileHandlerCallback,
			long timeoutMillis, long fileLengthCheckDelay) {
		return listen(directoryName, fileHandlerCallback, timeoutMillis, fileLengthCheckDelay, null);
	}

	public static WatchItem listen(String directoryName, FileHandlerCallback fileHandlerCallback,
			long timeoutMillis, long fileLengthCheckDelay, String fileName) {
		FolderListener listener;
		WatchItem item;
		
		LOGGER.debug("Looking for folder listener to folder \"{}\" ...", directoryName);
		synchronized (listeners) {
			for (int i = 0; i < listeners.size(); i++) {
				listener = listeners.get(i);
				if (listener.itemList.getDirectoryName().equals(directoryName)) {
					if (listener.isAlive()) {
						LOGGER.debug("Folder listener \"{}\" found. Append file handler for UUID {}",
								directoryName, fileHandlerCallback.getUUID());
						item = listener.itemList.append(fileHandlerCallback, timeoutMillis, fileLengthCheckDelay, fileName);
						return item;
					} else {
						LOGGER.warn("Folder listener \"{}\" is dead and will be removed", directoryName);
						listeners.remove(i);
						break;
					}
				}
			}
			LOGGER.debug("Folder listener \"{}\" not found. Create new listener and append file handler for UUID {}",
					directoryName, fileHandlerCallback.getUUID());
			listener = new FolderListener(directoryName);
			item = listener.itemList.append(fileHandlerCallback, timeoutMillis, fileLengthCheckDelay, fileName);
			listeners.add(listener);
			listener.start();
		}
		return item;
	}
	
	public static void terminateAll() {
		FolderListener[] array;
		synchronized (listeners) {
			array = listeners.toArray(new FolderListener[listeners.size()]);
		}
		for (int i = 0; i < array.length; i++) {
			array[i].terminate();
		}
	}
	
	public static List<FolderListener> getListeners() {
		return listeners;
	}
	
	public WatchItemList getItemList() {
		return itemList;
	}
	
	protected void start() {
		thread = new Thread(this, getClass().getSimpleName() + "-" + listeners.size());
		thread.start();
	}
	
	protected void terminate() {
		if (thread == null) {
			return;
		}
		try {
			thread.join(LISTENER_TIMEOUT_MILLLIS);
			LOGGER.debug("Folder listener \"{}\" will be terminate", getDirectoryName());
			thread.interrupt();
		} catch (InterruptedException e) {
			// Nothing
		}
	}
	
	public boolean isAlive() {
		return (thread != null ? thread.isAlive() : false);
	}
	
	public boolean isListen() {
		return isListen;
	}
	
	
	public String getDirectoryName() {
		return itemList.getDirectoryFullName();
	}
	
	private boolean waitForDirectory() throws InterruptedException {
    	File file = new File(getDirectoryName());
		if (file.exists()) {
    		return true;
    	} else {
    		LOGGER.debug("Folder listener \"{}\" expect to create it", getDirectoryName());
    	}

		long expiredTimeMillis = itemList.get(0).getExpiredTimeMillis();
		while (expiredTimeMillis >= System.currentTimeMillis()) {
    		Thread.sleep(WAIT_DIR_TIMEOUT_MILLIS);
    		if (file.exists()) {
        		return true;
        	}
    	}
    	return false;
    }

	private boolean init() {
		LOGGER.debug("Folder listener initializing for \"{}\" ...", getDirectoryName());
    	
		try {
    		if (!waitForDirectory()) {
    			LOGGER.error("Folder listener \"{}\" error. Timeout expired and directory not exists", getDirectoryName());
    			return false;
    		}
    	} catch (InterruptedException e) {
    		LOGGER.debug("Folder listener \"{}\" has been interrupted", getDirectoryName());
    		return false;
    	}

		processStatusItems();
		if (itemList.size() == 0) {
			LOGGER.debug("Folder listener \"{}\" have no files and will be finished", getDirectoryName());
			return false;
		}
		
		watcher = null;
		try {
			watcher = FileSystems.getDefault().newWatchService();
			Path dir = Paths.get(getDirectoryName());
			dir.register(watcher, ENTRY_CREATE);
		} catch (IOException e) {
			if (watcher != null) {
				try {
					watcher.close();
				} catch (IOException e1) {
					LOGGER.debug("Folder listener for \"{}\" closed with error.", getDirectoryName(), e1);
				}
				watcher = null;
			}
			throw new DlabException("Can't create folder listener for \"" + getDirectoryName() + "\".", e);
		}

		LOGGER.debug("Folder listener has been initialized for \"{}\" ...", getDirectoryName());
		return true;
	}
	
	private void processStatusItems() {
		int i = 0;
		
		itemList.processItemAll();
		while (i < itemList.size()) {
			final WatchItem item = itemList.get(i);
			final ItemStatus status = item.getStatus();
			final String uuid = item.getFileHandlerCallback().getUUID();
			
			switch (status) {
			case WAIT_FOR_FILE:
			case FILE_CAPTURED:
			case INPROGRESS:
				// Skip
				i++;
				continue;
			case TIMEOUT_EXPIRED:
				LOGGER.warn("Folder listener \"{}\" remove expired file handler for UUID {}", getDirectoryName(), uuid);
				break;
			case IS_DONE:
				LOGGER.debug("Folder listener \"{}\" remove processed file handler for UUID {}, handler result is {}", getDirectoryName(), uuid, item.getFutureResult());
				try {
					item.getFutureResultSync();
				} catch (InterruptedException e) {
					LOGGER.debug("Folder listener \"{}\" remove iterrupted file handler for UUID {}, {}", getDirectoryName(), uuid, e);
				} catch (ExecutionException e) {
					LOGGER.debug("Folder listener \"{}\" remove iterrupted file handler for UUID {}, {}", getDirectoryName(), uuid, e);
				}
				break;
			case IS_CANCELED:
				LOGGER.debug("Folder listener \"{}\" remove canceled file handler for UUID {}", getDirectoryName(), uuid);
				break;
			case IS_FAILED:
				LOGGER.debug("Folder listener \"{}\" remove failed file handler for UUID {}", getDirectoryName(), uuid);
				break;
			case IS_INTERRUPTED:
				LOGGER.debug("Folder listener \"{}\" remove iterrupted file handler for UUID {}", getDirectoryName(), uuid);
				break;
			default:
				continue;
			}
			itemList.remove(i);
		}
		
		if (itemList.size() > 0 ) {
			expiredIdleMillis = 0;
		} else if (expiredIdleMillis == 0) {
			expiredIdleMillis = System.currentTimeMillis() + LISTENER_IDLE_TIMEOUT_MILLLIS;
		}
	}
	
	private boolean removeListener(boolean force) {
		synchronized (listeners) {
			if (force || (itemList.size() == 0 && expiredIdleMillis < System.currentTimeMillis())) {
				for (int i = 0; i < listeners.size(); i++) {
					if (listeners.get(i) == this) {
						listeners.remove(i);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void pollFile() {
		try {
			while (true) {
				WatchKey key;
				LOGGER.trace("Folder listener \"{}\" poll attemt with {}", getDirectoryName(), watcher);
				isListen = true;
				key = watcher.poll(LISTENER_TIMEOUT_MILLLIS, TimeUnit.MILLISECONDS);
				LOGGER.trace("Folder listener \"{}\" polled", getDirectoryName());
	
				if (key != null) {
					for ( WatchEvent<?> event : key.pollEvents() ) {
						WatchEvent.Kind<?> kind = event.kind();
						if (kind == ENTRY_CREATE) {
							String fileName = event.context().toString();
							LOGGER.trace("Folder listener \"{}\" check file {}", getDirectoryName(), fileName);

							WatchItem item = itemList.getItem(fileName);
							if (item != null && item.getFileName() == null) {
								LOGGER.debug("Folder listener \"{}\" handle file {}", getDirectoryName(), fileName);
								item.setFileName(fileName);
								if (itemList.processItem(item)) {
									LOGGER.debug("Folder listener \"{}\" process file {}", getDirectoryName(), fileName);
								}
							}
						}
					}
					key.reset();
				}
				
				processStatusItems();
				if (removeListener(false)) {
					LOGGER.debug("Folder listener \"{}\" have no files and will be finished", getDirectoryName());
					break;
				}
			}
		} catch (InterruptedException e) {
			removeListener(true);
			LOGGER.debug("Folder listener \"{}\" has been interrupted", getDirectoryName());
		} catch (Exception e) {
			removeListener(true);
			LOGGER.error("Folder listener for \"{}\" closed with error.", getDirectoryName(), e);
			throw new DlabException("Folder listener for \"" + getDirectoryName() + "\" closed with error. " + e.getLocalizedMessage(), e);
		} finally {
			isListen = false;
			try {
				watcher.close();
			} catch (IOException e) {
				LOGGER.debug("Folder listener for \"{}\" closed with error. {} ", getDirectoryName(), e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void run() {
    	if (init()) {
			pollFile();
		} else {
			removeListener(true);
		}
	}
	
	public static void main(String[] args) throws Exception {
		/*FolderListener.listen("watch_dir", "DIS_for_Rostelecom_OP442838_POC_119243.key", 10000, false);
		FolderListener.listen("watch_dir2", "DIS_for_Rostelecom_OP442838_POC_119243.key", 3000, false);
		Thread.sleep(5000);
		FolderListener.listen("watch_dir", "2.key", 10000, false);
		FolderListener.listen("watch_dir2", "3.key", 3000, false);
		
		FolderListener.listen("watch_dir", "DIS_for_Rostelecom_OP442838_POC_119243.key", 10000, false);
		while ( true ) {
			Thread.sleep(10000);
		}*/
	}
	
}