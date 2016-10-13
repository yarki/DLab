package com.epam.dlab.auth.ldap.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;

public class ReturnableConnection implements Closeable {

	private final LdapConnectionPool pool;
	private LdapConnection con;
	private final Lock lock = new ReentrantLock();
	
	public ReturnableConnection(LdapConnectionPool pool) {
		Objects.requireNonNull(pool);
		this.pool = pool;
	}
	
	public LdapConnection getConnection() throws Exception {
		try {
			lock.lock(); //just protect from inproper use
			if(con == null) {
				con = pool.borrowObject();
			} else {
				throw new IllegalStateException("Cannot reuse connection. Create new ReturnableConnection");
			}
		} finally {
			lock.unlock();
		}
		return con;
	}
	
	@Override
	public void close() throws IOException {
		try {
			pool.releaseConnection(con);
		} catch (LdapException e) {
			throw new IOException("LDAP Release Connection error",e);
		}

	}

}
