/*********************************************************
*  Copyright (c) 2013 by Web Information Systems (WIS) Group.
*  Ke Tao, http://ktao.nl/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.io;

import nl.wisdelft.twinder.utility.PropertyReader;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

/**
 * This is an interface for getting access to the storage infrastructure powered by Apache 
 * Cassandra 2.0.7.
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 24, 2014
 */
public class CassandraUtility {

	private Cluster cluster;
	
	private Session session;
	
	public void connect(String node) {
		cluster = Cluster.builder()
				.addContactPoint(node)
				.build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n", 
				metadata.getClusterName());
		for ( Host host : metadata.getAllHosts() ) {
			System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}
		session = cluster.connect();
	}
	
	public void close() {
		session.close();
		cluster.close();
	}
	
	public void createSchema() {
		session.execute("CREATE KEYSPACE simplex WITH replication " + 
			      "= {'class':'SimpleStrategy', 'replication_factor':3};");
		
	}
	
	/**
	 * Just for testing purposes.
	 * @param args
	 */
	public static void main(String[] args) {
		CassandraUtility client = new CassandraUtility();
		client.connect(PropertyReader.getString("tal.db.cassandra.location"));
		//session.execute("SHOW HOST");
		client.createSchema();
		client.close();
	}
}
