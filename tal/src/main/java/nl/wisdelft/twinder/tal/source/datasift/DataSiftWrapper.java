/*********************************************************
*  Copyright (c) 2013 by Web Information Systems (WIS) Group.
*  Ke Tao, http://ktao.nl/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.tal.source.datasift;

import com.datasift.client.DataSiftClient;
import com.datasift.client.DataSiftConfig;

/**
 * This wrapper aims at getting DataSift streams as sources for analyses in TAL. 
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 22, 2014
 */
public class DataSiftWrapper {

	/** The DataSift client instance */
	private DataSiftClient client;
	
	/**
	 * Construtor method of DataSift Client wrapper class.
	 * @param username The username to be passed to DataSift.
	 * @param key The API key assigned by DataSift.
	 */
	public DataSiftWrapper(String username, String key) {
		System.out.println("Trying to get client instance with username " + username);
		DataSiftConfig config = new DataSiftConfig(username, key);
		this.client = new DataSiftClient(config);
		// TODO check if the connection has been established successfully.
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("This is the wrapper class for using DataSift Java API.");
		
		DataSiftWrapper wrapper = new DataSiftWrapper("ktao.gmail", 
				"e34f541ee25ecaf7b83093282b68bec2");
		DataSiftClient client = wrapper.getClient();
		System.out.println(client);
	}
	
	/**
	 * @return The DataSift client object.
	 */
	public DataSiftClient getClient() {
		return client;
	}
}
