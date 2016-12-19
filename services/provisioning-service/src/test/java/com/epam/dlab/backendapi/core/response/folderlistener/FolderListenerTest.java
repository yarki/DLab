package com.epam.dlab.backendapi.core.response.folderlistener;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListener;
import com.epam.dlab.backendapi.core.response.folderlistener.WatchItem;

public class FolderListenerTest {
	
	private static final String UUID = "123";

	private final FileHandlerCallback fHandler = new FileHandler(UUID);
	
	private final long timeoutMillis = 1000;

	private final long fileLengthCheckDelay = 1000;
	
	private boolean handleResult = true;
	
	public class FileHandler implements FileHandlerCallback {
		private final String uuid;

		public FileHandler(String uuid) {
			this.uuid = uuid;
		}

		@Override
		public String getUUID() {
			return uuid;
		}

		@Override
		public boolean checkUUID(String uuid) {
			return this.uuid.equals(uuid);
		}

		@Override
		public boolean handle(String fileName, byte[] content) throws Exception {
			if (!handleResult) {
				throw new Exception("Test exception");
			}
			return handleResult;
		}

		@Override
		public void handleError() {
			System.out.println("handleError called for UUID " + getUUID());
		}
	}

	private String getFileName() {
		return UUID + ".json";
	}

	private String getDirectory() {
		return "./";
	}
	
	private void createFile() throws IOException {
		File file = new File(getDirectory(), getFileName());
		if ( file.exists() ) {
			file.delete();
		}
		FileWriter f = new FileWriter(file);
		
		f.write("test");
		f.flush();
		f.close();
	}
	
	
	private void processFile(WatchItem item) throws InterruptedException, IOException {
		while (!FolderListener.getListeners().isEmpty() && !FolderListener.getListeners().get(0).isListen()) {
			Thread.sleep(100);
		}
		createFile();
		while (item.getFuture() == null) {
			Thread.sleep(100);
		}
	}
	
	@Test
	public void listen() throws InterruptedException, ExecutionException, IOException {
		WatchItem item;
		
		handleResult = false;
		item = FolderListener.listen(getDirectory(), fHandler, timeoutMillis, fileLengthCheckDelay);
		FolderListener listener = FolderListener.getListeners().get(0);
		assertEquals(false, listener.isListen());
		assertEquals(true, listener.isAlive());
		
		System.out.println("TEST process FALSE");
		processFile(item);
		assertEquals(true, listener.isListen());
		assertEquals(false, item.getFutureResultSync());
		assertEquals(false, item.getFutureResult());

		System.out.println("TEST process TRUE");
		handleResult = true;
		item = FolderListener.listen(getDirectory(), fHandler, timeoutMillis, fileLengthCheckDelay);
		processFile(item);
		assertEquals(true, item.getFutureResultSync());
		assertEquals(true, item.getFutureResult());
		
		System.out.println("TEST process with out listen");
		createFile();
		item = FolderListener.listen(getDirectory(), fHandler, timeoutMillis, fileLengthCheckDelay, getFileName());
		while (item.getFuture() == null) {
			Thread.sleep(100);
		}
		assertEquals(true, item.getFutureResultSync());
		assertEquals(true, item.getFutureResult());

		//listener = FolderListener.getListeners().get(0);
		//listener.terminate();
		FolderListener.terminateAll();

		Thread.sleep(1000);
	}

}
