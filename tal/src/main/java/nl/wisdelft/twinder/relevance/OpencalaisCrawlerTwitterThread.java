/*********************************************************
*  Copyright (c) 2010 by Web Information Systems (WIS) Group.
*  Fabian Abel, http://fabianabel.de/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.relevance;

import java.sql.ResultSet;
import java.sql.Types;

import mx.bigdata.jcalais.CalaisClient;
import mx.bigdata.jcalais.CalaisObject;
import mx.bigdata.jcalais.CalaisResponse;
import mx.bigdata.jcalais.rest.CalaisRestClient;
import nl.wisdelft.twinder.io.JDBCUtility;
import nl.wisdelft.twinder.utility.TweetUtility;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

/**
 * Extract semantics from tweets via OpenCalais.
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @author last edited by: $Author: $
 * 
 * @version created on Apr 4, 2011
 * @version $Revision: $ $Date: $
 */
public class OpencalaisCrawlerTwitterThread extends Thread {
	
	/** the query that will return the tweets to be processed */
	public String query = null; 
	
	public String API_KEY = null; 
	
	/** ID of the user to be processed */
	public int userId = -1;

	/**
	 * @param query
	 * @param aPI_KEY
	 * @param userId
	 */
	public OpencalaisCrawlerTwitterThread(String query, String aPI_KEY) {
		super();
		this.query = query;
		API_KEY = aPI_KEY;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		ResultSet tweets = null;

		tweets = JDBCUtility.executeQuerySingleConnection(query);
		try{
			String content = null;
			Long tweetId = null;
			
			CalaisClient client = new CalaisRestClient(API_KEY);
		    CalaisResponse response = null;
		    
			BatchSqlUpdate bsu = new BatchSqlUpdate(
					JDBCUtility.ds,
					"INSERT IGNORE INTO semanticsTweetsEntity (tweetId, type, typeURI, name, uri, relevance) "
							+ " values (?,?,?,?,?,?)");
			bsu.declareParameter(new SqlParameter("tweetId", Types.BIGINT)); //$NON-NLS-1$
			bsu.declareParameter(new SqlParameter("type", Types.VARCHAR)); //$NON-NLS-1$
			bsu.declareParameter(new SqlParameter("typeURI", Types.VARCHAR)); //$NON-NLS-1$
			bsu.declareParameter(new SqlParameter("name", Types.VARCHAR)); //$NON-NLS-1$
			bsu.declareParameter(new SqlParameter("uri", Types.VARCHAR)); //$NON-NLS-1$
			bsu.declareParameter(new SqlParameter("relevance", Types.DOUBLE)); //$NON-NLS-1$
			bsu.compile();
			
			BatchSqlUpdate bsu_topic = new BatchSqlUpdate(
					JDBCUtility.ds,
					"INSERT IGNORE INTO semanticsTweetsTopic (tweetId, topic, uri, relevance) "
							+ " values (?,?,?,?)");
			bsu_topic.declareParameter(new SqlParameter("tweetId", Types.BIGINT)); //$NON-NLS-1$
			bsu_topic.declareParameter(new SqlParameter("topic", Types.VARCHAR)); //$NON-NLS-1$
			bsu_topic.declareParameter(new SqlParameter("uri", Types.VARCHAR)); //$NON-NLS-1$
			bsu_topic.declareParameter(new SqlParameter("relevance", Types.DOUBLE)); //$NON-NLS-1$
			bsu_topic.compile();
			
			//iterate over all news to make OpenCalais Web Service Call and store the entities, etc.
			int count = 0; int storeAfter = 50;
			while (tweets.next()) {
				try{
					count++;
					content = TweetUtility.removeURLs(tweets.getString("content"));
					tweetId = tweets.getLong("id");
					
					System.out.println("Processing Tweet " + tweetId);
					
					//make OpenCalais Web service call:
					response = client.analyze(content);
					
					//store entities:
					try{
						if(response.getEntities() != null){
							for (CalaisObject entity : response.getEntities()) {
								//store news entity assignment:
								Object[] toadd = {
									tweetId,
									entity.getField("_type"),
									entity.getField("_typeReference"),
									entity.getField("name"),
									entity.getField("_uri"),
									(entity.getField("relevance") != null ? entity.getField("relevance") : 0.0),
								};
								bsu.update(toadd);
							}
						}
					}catch (Exception e) {
						System.err.println("Problems while storing tweet entity assignments: " + e.getMessage());
					}
					
					//store topics:
					try{
						if(response.getTopics() != null){
							for (CalaisObject topic : response.getTopics()) {
								Object[] toadd = {
										tweetId,
										topic.getField("categoryName"),
										topic.getField("category"),
										(topic.getField("score") != null ? topic.getField("score") : 0.0),
									};
									bsu_topic.update(toadd);
							}
						}
					}catch (Exception e) {
						System.err.println("Problems while stroing tweet topic assignments: " + e.getMessage());
					}
					
					if(count % storeAfter == 0){
						bsu.flush();
						bsu_topic.flush();
						System.out.println("\n*********************\n* Processed " + count + " tweets.\n**********************\n");
					}
				}catch (Exception e) {
					e.printStackTrace();
					if(e.getMessage() != null && e.getMessage().contains("returned a response status of 403") && e.getMessage().toLowerCase().contains("response")){
						API_KEY = OpencalaisCrawler.switchAPIKey(API_KEY);
						client = new CalaisRestClient(API_KEY);
						System.out.println("####\n#### New API key: " + API_KEY);
					}
				}
			}
			
			bsu.flush();
			bsu_topic.flush();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
