/*********************************************************
*  Copyright (c) 2011 by Web Information Systems (WIS) Group.
*  Ke Tao, http://taubau.info/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.tal.model.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Set;

import nl.wisdelft.twinder.io.JDBCUtility;

/**
 * This class is supposed to expand the queries based on the related tweets we got from original query.
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Aug 5, 2011
 */
public class IndriTweetExpandedQueryGenerator {
	/**
	 * The time constraint of selecting relating tweets
	 * Default : 345600 seconds (The unit of millisecond is not supported by MySQL)
	 */
	 private static int duration = 345600;
	
	/** Top K dbpedia Entities will be selected to generate the new query */
	private static int topK = 10;
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static DecimalFormat df2p = new DecimalFormat("#.##");
	
	public static void main(String[] args) {
		weightedLabels(args);
	}
	
	public static void weightedLabels(String[] args) {
		/** Result ID is expected to be given by parameter from... */
		int resultId = Integer.parseInt(args[0]);
		
		/** The extended query file is named after the 2nd parametre.*/
		String filename = args[1];
		
		if(args.length == 3) {
			duration = Integer.parseInt(args[2]);
		}
				
		// select related tweets and extracted entities. aggregate them and make query.
		String sql = "SELECT topicGroup, annotated, stemmed FROM indriResult WHERE id = " + resultId;
		
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(sql);
		
		String topicGroup = "";
		
		try {
			File queryFile = new File("./Xquery/" + filename);
			FileOutputStream queryFileOS = new FileOutputStream(queryFile);

			queryFileOS.write("<parameters>\n".getBytes());
			
			if(rs.next()) {
				topicGroup = rs.getString(1);
				
				sql = "SELECT id, queryTime, title FROM topic WHERE topicGroup = '" + topicGroup + "'";
				
				ResultSet topicIds = JDBCUtility.executeQuerySingleConnection(sql);
								
				while(topicIds.next()) {
					int topicId = topicIds.getInt(1);
					
					String topicTitle = topicIds.getString(3);
					
					queryFileOS.write("<query>\n".getBytes());
					queryFileOS.write(("<type>indri</type>\n").getBytes());
					queryFileOS.write(("<number>" + topicId + "</number>\n").getBytes());
					queryFileOS.write("<text>\n".getBytes());
					
					// First half
					String origin = "#weight( 0.75 #combine ( ";
					
					sql = "SELECT a.annotatedText, a.DBpediaURI " +
							"FROM dbpediaEntityInTopic a, indriResult b " +
							"WHERE b.id = " + resultId + " AND a.topicId = " + topicId + " " + 
							"AND a.confidence = b.confidence AND a.support = b.support AND a.topicGroup = b.topicGroup;";
					
					ResultSet originLabels = JDBCUtility.executeQuerySingleConnection(sql);
					
					while(originLabels.next()) {
						String annotatedText = originLabels.getString(1);
						if(annotatedText.contains(" ")) {
							topicTitle += " #1(" + annotatedText + ") ";
						}
					}
					
					topicTitle = QueryUtility.checkSpace(topicTitle);
					origin += topicTitle;
					origin += ")\n";
					
					// Second half
					String extension = "0.25 #combine ( #wsum ( ";
					
					Timestamp queryTime = topicIds.getTimestamp(2);
					
					sql = "SELECT a.annotatedText, a.dbpediaURI, count(*) c " +
							"FROM dbpediaEntity a, " +
								"(SELECT tweets.id, tweets.content, indriResultEntry.topicId, indriResultEntry.resultId " +
								"FROM tweets, indriResultEntry " +
								"WHERE tweets.id = indriResultEntry.tweetId AND " +
									"indriResultEntry.topicId = " + topicId + " AND " +
									"indriResultEntry.resultId = " + resultId  + " AND " +
									"UNIX_TIMESTAMP(tweets.creationTime) < UNIX_TIMESTAMP('" + df.format(queryTime)+ "') + " + duration + " AND " +
									"UNIX_TIMESTAMP(tweets.creationTime) > UNIX_TIMESTAMP('" + df.format(queryTime)+ "') - " + duration + ") b " +
							"WHERE b.id = a.tweetId GROUP BY a.dbpediaURI ORDER BY c DESC LIMIT " + topK;
					
					ResultSet labels = JDBCUtility.executeQuerySingleConnection(sql);
					
					HashMap<String, Integer> entityOccurence = new HashMap<String, Integer>();
					
					int totalOccurences = 0;
					
					while(labels.next()) {
//						if(annotated) {
//							String annotatedText = labels.getString(1);
//							extension += QueryUtility.checkSpace(annotatedText) + " ";
//						} else {
						// normalize the weight, and give topK entities weight
						String dbpediaURI = labels.getString(2);
						int occurence = labels.getInt(3);
						dbpediaURI = QueryUtility.dbpediaURItoLabel(dbpediaURI);
						
						totalOccurences += occurence; 
						
						Integer oldOccurence = entityOccurence.put(dbpediaURI, occurence);
						
						if(oldOccurence != null) {
							entityOccurence.put(dbpediaURI, occurence + oldOccurence);
						}
					}
					
					Set<String> keys = entityOccurence.keySet();
					
					for(String key : keys) {
						double ratio = (double) entityOccurence.get(key) / (double) totalOccurences;
						extension += df2p.format(ratio) + " " + QueryUtility.dbpediaURItoLabel(key, true) + " ";
					}
					
					// generate the extension of the ....
					
					extension += ") ) )\n";
					
					queryFileOS.write((origin + extension).getBytes());
					
					queryFileOS.write("</text>\n".getBytes());
					queryFileOS.write("</query>\n\n".getBytes());
					
					System.out.println("Topic " + topicId + " completed.");
				}
			} else {
				return;
			}			
			queryFileOS.write("</parameters>\n".getBytes());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void spotlightBased(String[] args) {
		/** Result ID is expected to be given by parameter from... */
		int resultId = Integer.parseInt(args[0]);
		
		/** The extended query file is named after the 2nd parametre.*/
		String filename = args[1];
		
		if(args.length == 3) {
			duration = Integer.parseInt(args[2]);
		}
				
		// select related tweets and extracted entities. aggregate them and make query.
		String sql = "SELECT topicGroup, annotated, stemmed FROM indriResult WHERE id = " + resultId;
		
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(sql);
		
		String topicGroup = "";
		boolean annotated = false;
		
		try {
			File queryFile = new File("./Xquery/" + filename);
			FileOutputStream queryFileOS = new FileOutputStream(queryFile);

			queryFileOS.write("<parameters>\n".getBytes());
			
			if(rs.next()) {
				topicGroup = rs.getString(1);
				annotated = rs.getBoolean(2);
				
				sql = "SELECT id, queryTime FROM topic WHERE topicGroup = '" + topicGroup + "'";
				
				ResultSet topicIds = JDBCUtility.executeQuerySingleConnection(sql);
								
				while(topicIds.next()) {
					int topicId = topicIds.getInt(1);
					
					queryFileOS.write("<query>\n".getBytes());
					queryFileOS.write(("<type>indri</type>\n").getBytes());
					queryFileOS.write(("<number>" + topicId + "</number>\n").getBytes());
					queryFileOS.write("<text>\n".getBytes());
					
					// First half
					String origin = "#weight( 0.75 #combine ( ";
					
					sql = "SELECT a.annotatedText, a.DBpediaURI " +
							"FROM dbpediaEntityInTopic a, indriResult b " +
							"WHERE b.id = " + resultId + " AND a.topicId = " + topicId + " " + 
							"AND a.confidence = b.confidence AND a.support = b.support AND a.topicGroup = b.topicGroup;";
					
					ResultSet originLabels = JDBCUtility.executeQuerySingleConnection(sql);
					
					while(originLabels.next()) {
						if(annotated) {
							String annotatedText = originLabels.getString(1);
							origin += QueryUtility.checkSpace(annotatedText) + " ";	
						} else {
							String dbpediaURI = originLabels.getString(2);
							dbpediaURI = QueryUtility.dbpediaURItoLabel(dbpediaURI);
							origin += QueryUtility.checkSpace(dbpediaURI) + " ";
						}
					}
					origin += ")\n";
					
					// Second half
					String extension = "0.25 #combine ( ";
					
					Timestamp queryTime = topicIds.getTimestamp(2);
					
					sql = "SELECT a.annotatedText, a.dbpediaURI, count(*) c " +
							"FROM dbpediaEntity a, " +
								"(SELECT tweets.id, tweets.content, indriResultEntry.topicId, indriResultEntry.resultId " +
								"FROM tweets, indriResultEntry " +
								"WHERE tweets.id = indriResultEntry.tweetId AND " +
									"indriResultEntry.topicId = " + topicId + " AND " +
									"indriResultEntry.resultId = " + resultId  + " AND " +
									"UNIX_TIMESTAMP(tweets.creationTime) < UNIX_TIMESTAMP('" + df.format(queryTime)+ "') + " + duration + " AND " +
									"UNIX_TIMESTAMP(tweets.creationTime) > UNIX_TIMESTAMP('" + df.format(queryTime)+ "') - " + duration + ") b " +
							"WHERE b.id = a.tweetId GROUP BY a.dbpediaURI ORDER BY c DESC LIMIT " + topK;
										
					ResultSet labels = JDBCUtility.executeQuerySingleConnection(sql);
					
					while(labels.next()) {
						if(annotated) {
							String annotatedText = labels.getString(1);
							extension += QueryUtility.checkSpace(annotatedText) + " ";
						} else {				
							String dbpediaURI = labels.getString(2);
							dbpediaURI = QueryUtility.dbpediaURItoLabel(dbpediaURI);
							extension += QueryUtility.checkSpace(dbpediaURI) + " ";
						}
					}
					extension += "))\n";
					
					queryFileOS.write((origin + extension).getBytes());
					
					queryFileOS.write("</text>\n".getBytes());
					queryFileOS.write("</query>\n\n".getBytes());
					
					System.out.println("Topic " + topicId + " completed.");
				}
			} else {
				return;
			}			
			queryFileOS.write("</parameters>\n".getBytes());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
