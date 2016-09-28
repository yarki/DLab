package com.epam.datalab.auth.ldap;

import java.util.Scanner;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.PoolableLdapConnectionFactory;

public class BasicTest {

	public static void main(String[] args) throws Exception {
		System.out.println("Basic test");
		LdapConnectionConfig config = new LdapConnectionConfig();
		config.setLdapHost( "localhost" );
		config.setLdapPort( 3890 );
		config.setName( "cn=admin,dc=example,dc=com" );
		config.setCredentials( "ldap" );
		PoolableLdapConnectionFactory factory = new PoolableLdapConnectionFactory( config );
		LdapConnectionPool pool = new LdapConnectionPool( factory );
		pool.setTestOnBorrow( true );
		LdapConnection con = pool.borrowObject();
		
		SearchRequest sr = new SearchRequestImpl();
	    sr.setScope(SearchScope.SUBTREE);
	    sr.addAttributes("*");
	    sr.setTimeLimit(0);
	    sr.setBase(new Dn("dc=example,dc=com"));
	    sr.setFilter("(cn=Mike Teplitskiy)");
	    sr.setMessageId(1);
		
//		EntryCursor cursor = con.search( "dc=example,dc=com", "(objectclass=*)", SearchScope.SUBTREE );
		SearchCursor cursor = con.search( sr );
//
//		cursor.forEach(entry->{
//	    	System.out.println( "---- DN "+entry.getDn() );
//		    entry.forEach(attr->{
//		    	System.out.println( "---- ATTR "+attr );
//		    });
//			
//		});
		
		cursor.forEach(response->{
		    if ( response instanceof SearchResultEntry )
		    {
		        Entry resultEntry = ( ( SearchResultEntry ) response ).getEntry();
		        System.out.println( "---- DN "+resultEntry.getDn() );
		        resultEntry.forEach(attr->{
		        	System.out.println( "---- ATTR "+attr );
		        });
		    }
		});
		cursor.close();
		
	    sr.setFilter("(cn=John Doe)");
	    sr.setMessageId(1);
	    cursor = con.search( sr );
		cursor.forEach(response->{
		    if ( response instanceof SearchResultEntry )
		    {
		        Entry resultEntry = ( ( SearchResultEntry ) response ).getEntry();
		        System.out.println( "---- DN "+resultEntry.getDn() );
		        resultEntry.forEach(attr->{
		        	System.out.println( "---- ATTR "+attr );
		        });
		    }
		});
		cursor.close();
	    

		
		con.unBind();
		
		con.bind("uid=mike,ou=People,dc=example,dc=com","miket");
	    sr.setFilter("(uid=mike)");
	    sr.setMessageId(2);
	    cursor = con.search( sr );
		cursor.forEach(response->{
		    if ( response instanceof SearchResultEntry )
		    {
		        Entry resultEntry = ( ( SearchResultEntry ) response ).getEntry();
		        System.out.println( "---- DN "+resultEntry.getDn() );
		        resultEntry.forEach(attr->{
		        	System.out.println( "---- ATTR "+attr );
		        });
		    }
		});
		cursor.close();
		
		System.out.println("Press ENTER");
		pool.releaseConnection(con);
		pool.close();
		
	}

}
