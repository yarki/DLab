package com.epam.dlab.backendapi.core.response.folderlistener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.epam.dlab.backendapi.core.FileHandlerCallback;

/** Class to store a file processing.
 * @author Usein_Faradzhev
 */
public class WatchItem implements Comparable<WatchItem> {
	
	/** Status of file processing.
	 * <pre>
	 * WAIT_FOR_FILE waiting for file creation.
	 * TIMEOUT_EXPIRED timeout expired for file creation.
	 * FILE_CAPTURED file created and handled.
	 * INPROGRESS file processing is running.
	 * IS_DONE file processing is done. Check result of processing {@link WatchItem.getFutureResult()}
	 * IS_CANCELED file processing was canceled.
	 * IS_INTERRUPTED file processing was interrupted.
	 * IS_FAILED file processing is failed.
	 * </pre>
	 *  */
	public enum ItemStatus {
		WAIT_FOR_FILE,
		TIMEOUT_EXPIRED,
		FILE_CAPTURED,
		INPROGRESS,
		IS_DONE,
		IS_CANCELED,
		IS_INTERRUPTED,
		IS_FAILED
	};
	
	/** File handler for processing. */
	private final FileHandlerCallback fileHandlerCallback;
	/** Timeout waiting for file creation in milliseconds. */
    private final long timeoutMillis;
    /** Timeout waiting for file writing in milliseconds. */
    private final long fileLengthCheckDelay;

    /** Time expired for file creation in milliseconds. */
    private long expiredTimeMillis;
    /** File name. */
	private String fileName;
	/** Future for async file processing. */
	private CompletableFuture<Boolean> future;
	/** Result of file processing. */
	private Boolean futureResult = null;

	/** Creates instance of file handler.
	 * @param fileHandlerCallback File handler for processing.
	 * @param timeoutMillis Timeout waiting for file creation in milliseconds.
	 * @param fileLengthCheckDelay Timeout waiting for file writing in milliseconds.
	 */
	public WatchItem(FileHandlerCallback fileHandlerCallback, long timeoutMillis, long fileLengthCheckDelay) {
		this.fileHandlerCallback = fileHandlerCallback;
		this.timeoutMillis = timeoutMillis;
		this.fileLengthCheckDelay = fileLengthCheckDelay;
	    setExpiredTimeMillis(timeoutMillis);
	}

	@Override
	public int compareTo(WatchItem o) {
		if (o == null) {
			return -1;
		}
		return (fileHandlerCallback.checkUUID(o.fileHandlerCallback.getUUID()) ?
					0 : fileHandlerCallback.getUUID().compareTo(o.fileHandlerCallback.getUUID()));
	}
	
	/** Returns file handler for processing. */
	public FileHandlerCallback getFileHandlerCallback() {
		return fileHandlerCallback;
	}
	
	/** Returns timeout waiting for file creation in milliseconds. */
	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	/** Returns timeout waiting for file writing in milliseconds. */
	public long getFileLengthCheckDelay() {
		return fileLengthCheckDelay;
	}


	/** Returns time expired for file creation in milliseconds. */
	public long getExpiredTimeMillis() {
		return expiredTimeMillis;
	}
	
	/** Sets time expired for file creation in milliseconds.
	 * @param timeout time expired for file creation in milliseconds. */
	private void setExpiredTimeMillis(long expiredTimeMillis) {
		this.expiredTimeMillis = System.currentTimeMillis() + expiredTimeMillis;
	}

	/** Returns file name. */
	public String getFileName() {
		return fileName;
	}

	/** Sets file name.
	 * @param fileName file name.
	 */
	protected void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/** Returns status of file processing.
	 *  See {@link ItemStatus} for details. */
    public ItemStatus getStatus() {
		if (fileName == null) {
    		return (expiredTimeMillis < System.currentTimeMillis() ? ItemStatus.TIMEOUT_EXPIRED : ItemStatus.WAIT_FOR_FILE); 
    	} else if (future == null) {
    		return ItemStatus.FILE_CAPTURED;
    	} else if (future.isCancelled()) {
    		return ItemStatus.IS_CANCELED;
    	}
    	
    	if (future.isDone()) {
    		try {
				futureResult = future.get();
				return ItemStatus.IS_DONE;
			} catch (InterruptedException e) {
				return ItemStatus.IS_INTERRUPTED;
			} catch (ExecutionException e) {
				return ItemStatus.IS_FAILED;
			}
    	}
    	
    	return ItemStatus.INPROGRESS;
    }

	/** Returns true if time expired for file creation. */
	public boolean isExpired() {
		return (fileName == null && expiredTimeMillis < System.currentTimeMillis());
	}


	/** Returns future for async file processing. */
	public CompletableFuture<Boolean> getFuture() {
		return future;
	}
	
	/** Sets future for file processing.
	 * @param future completable future for file processing. 
	 */
	protected void setFuture(CompletableFuture<Boolean> future) {
		this.future = future;
	}
	
	/** Returns result of file processing. This method is non-blocking and returns true or false
      * if file processing was done, otherwise returns null. */
	public Boolean getFutureResult() {
		if (futureResult == null && future != null && future.isDone()) {
			try {
				futureResult = future.get();
			} catch (Exception e) { }
		}
		return futureResult; 
	}
	
	/** Returns result of file processing. This method is blocking and returns true or false
      * when file processing have done. */
	public Boolean getFutureResultSync() throws InterruptedException, ExecutionException {
		if (futureResult == null && future != null) {
			futureResult = future.get();
		}
		return futureResult; 
	}
}
