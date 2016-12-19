package com.epam.dlab.backendapi.core.response.folderlistener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.epam.dlab.backendapi.core.FileHandlerCallback;

public class WatchItem implements Comparable<WatchItem> {
	
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
	
	private final FileHandlerCallback fileHandlerCallback;
    private final long timeoutMillis;
    private final long fileLengthCheckDelay;

    private long expiredTimeMillis;
	private String fileName;
	private CompletableFuture<Boolean> future;
	private Boolean futureResult = null;

	public WatchItem(FileHandlerCallback fileHandlerCallback, long timeoutMillis, long fileLengthCheckDelay) {
		this.fileHandlerCallback = fileHandlerCallback;
		this.timeoutMillis = timeoutMillis;
		this.fileLengthCheckDelay = fileLengthCheckDelay;
	    setExpiredTimeMillis(timeoutMillis);
	}

	public int compareTo(WatchItem o) {
		if (o == null) {
			return -1;
		}
		return (fileHandlerCallback.checkUUID(o.fileHandlerCallback.getUUID()) ?
					0 : fileHandlerCallback.getUUID().compareTo(o.fileHandlerCallback.getUUID()));
	}
	
	
	public FileHandlerCallback getFileHandlerCallback() {
		return fileHandlerCallback;
	}
	
	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	public long getFileLengthCheckDelay() {
		return fileLengthCheckDelay;
	}


	public long getExpiredTimeMillis() {
		return expiredTimeMillis;
	}
	
	private void setExpiredTimeMillis(long timeout) {
		expiredTimeMillis = System.currentTimeMillis() + timeout;
	}

	public String getFileName() {
		return fileName;
	}

	// TODO: Change to protected
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

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

	public boolean isExpired() {
		return (fileName == null && expiredTimeMillis < System.currentTimeMillis());
	}
	

	public CompletableFuture<Boolean> getFuture() {
		return future;
	}
	
	protected void setFuture(CompletableFuture<Boolean> future) {
		this.future = future;
	}
	
	public Boolean getFutureResult() {
		if (futureResult == null && future != null && future.isDone()) {
			try {
				futureResult = future.get();
			} catch (Exception e) { }
		}
		return futureResult; 
	}
	
	public Boolean getFutureResultSync() throws InterruptedException, ExecutionException {
		if (futureResult == null && future != null) {
			futureResult = future.get();
		}
		return futureResult; 
	}
}
