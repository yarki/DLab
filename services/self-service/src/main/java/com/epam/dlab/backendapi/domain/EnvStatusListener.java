package com.epam.dlab.backendapi.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.backendapi.dao.EnvStatusDAO;
import com.epam.dlab.mongo.MongoService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;

@Singleton
public class EnvStatusListener implements Managed, Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(EnvStatusListener.class);
	
	private static EnvStatusListener listener;
		
	public static synchronized void listen(String username) {
		LOGGER.debug("EnvStatus listener will be added the status check for user {}", username);
		listener.userMap.put(username, System.currentTimeMillis());
		if (listener.thread == null) {
			LOGGER.debug("EnvStatus listener not running and will be started ...");
			listener.thread = new Thread(listener, listener.getClass().getSimpleName());
			listener.thread.start();
		}
	}
	
	public static void listenStop(String username) {
		LOGGER.debug("EnvStatus listener will be removed the status check for user {}", username);
		synchronized (listener.userMap) {
			listener.userMap.remove(username);
			if (listener.userMap.size() == 0) {
				LOGGER.debug("EnvStatus listener will be terminated because no have the checkers anymore");
				try {
					listener.stop();
				} catch (Exception e) {
					LOGGER.warn("EnvStatus listener terminating failed: {}", e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	/** Thread of the folder listener. */
	private Thread thread;

	private long checkStatusTimeoutMillis;
	
	@Inject
	private SelfServiceApplicationConfiguration configuration;
	
	@Inject
	private EnvStatusDAO dao;
	
	private Map<String, Long> userMap = new HashMap<String, Long>();
	
	@Override
	public void start() throws Exception {
		if (listener == null) {
			listener = this;
			checkStatusTimeoutMillis = configuration.getCheckEnvStatusTimeout().toMilliseconds();
		}
	}

	@Override
	public void stop() throws Exception {
		if (listener.thread != null) {
			LOGGER.debug("EnvStatus listener will be stopped ...");
			synchronized (listener.thread) {
				listener.thread.interrupt();
				listener.thread = null;
				listener.userMap.clear();
			}
			LOGGER.debug("EnvStatus listener has been stopped");
		}
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				long ticks = System.currentTimeMillis();
				for (Entry<String, Long> item : userMap.entrySet()) {
					if (item.getValue() < ticks) {
						LOGGER.debug("EnvStatus listener check status for user {}" + item.getKey());
						item.setValue(ticks + checkStatusTimeoutMillis);
					}
				}
				
				synchronized (userMap) {
					if (userMap.isEmpty()) {
						thread = null;
						LOGGER.debug("EnvStatus listener has been stopped because no have the checkers anymore");
						return;
					}
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.debug("EnvStatus listener has been interrupted");
				break;
			} catch (Exception e) {
				LOGGER.warn("EnvStatus listener unhandled error: {}", e.getLocalizedMessage(), e);
			}
		}
	}

	
	private static Injector createInjector(SelfServiceApplicationConfiguration configuration, Environment environment) {
		return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(SelfServiceApplicationConfiguration.class).toInstance(configuration);
                bind(MongoService.class).toInstance(configuration.getMongoFactory().build(environment));
            }
        });
    }
	
	public static void main(String[] args) throws Exception {
		EnvStatusListener statuslistener = new EnvStatusListener();
		//Injector injector = createInjector();
		//injector.injectMembers(statuslistener);
		statuslistener.start();
		
		EnvStatusListener.listen("user1");
		Thread.sleep(1500);
		EnvStatusListener.listen("user2");
		
		Thread.sleep(10000);
	}

}
