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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.wisdelft.twinder.io.JDBCUtility;

/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Aug 3, 2011
 */
public class IndriQueryGenerator {
	
	public static void generateQueryFile(QueryGeneration queryModelingStrategy, String topicGroup, String outputFilePrefix){
		ResultSet topics = JDBCUtility.executeQuerySingleConnection("SELECT id, reference, topicGroup, title, queryTime, queryTweetTime FROM topic_tweets2011 WHERE topicGroup = \"" + topicGroup + "\" ");
		try{
			QueryProfile profile = null;
			Topic topic = null;
			FileOutputStream queryFileOS = new FileOutputStream(new File("./query/" + outputFilePrefix + "-" + queryModelingStrategy.getName() + ".query"));
			queryFileOS.write("<parameters>\n".getBytes());
			DecimalFormat formatter = new DecimalFormat("0.00000");
			while(topics.next()){
				topic = new Topic(
						topics.getInt("id"), 
						topics.getString("reference"), 
						topics.getString("topicGroup"),
						topics.getString("title"),
						topics.getTimestamp("queryTime"),
						topics.getLong("queryTweetTime")
						);
				//get query profile:
				profile = queryModelingStrategy.generateQuery(topic);
				
				//write to file:
				queryFileOS.write("<query>\n".getBytes());
				queryFileOS.write(("<type>indri</type>\n").getBytes());
				queryFileOS.write(("<number>" + topic.reference + "</number>\n").getBytes());
				queryFileOS.write("<text>\n".getBytes());
				queryFileOS.write(("#weight( ").getBytes());
				
				for(WeightedTerm wt: profile){
					queryFileOS.write(("" + formatter.format(wt.weight) + " #1(" + wt.term + ") ").getBytes());
					
				}
				queryFileOS.write(")".getBytes());
				queryFileOS.write("\n</text>\n".getBytes());
				queryFileOS.write("</query>\n\n".getBytes());
			}
			queryFileOS.write("</parameters>\n".getBytes());
			queryFileOS.flush();
			queryFileOS.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ke's version for generating query files.
	 * @param args 
	 */
	public static void generateProfilesAndQueryFiles(String[] args){
		String filename = args[0];
		double confidence = Double.parseDouble(args[1]);
		int support = Integer.parseInt(args[2]);
		String topicGroup = args[3];
		
		String sql = "SELECT id, title FROM topic WHERE topicGroup = '" + topicGroup + "'"; 
			
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(sql);
		
		try {
			File queryFile = new File("./query/" + filename);
			FileOutputStream queryFileOS = new FileOutputStream(queryFile);
					
			queryFileOS.write("<parameters>\n".getBytes());
			
			while(rs.next()) {
				int topicId = rs.getInt(1);
				String title = rs.getString(2);
				
				queryFileOS.write("<query>\n".getBytes());
				queryFileOS.write(("<type>indri</type>\n").getBytes());
				queryFileOS.write(("<number>" + topicId + "</number>\n").getBytes());
				queryFileOS.write("<text>\n".getBytes());
				
				sql = "SELECT annotatedText, dbpediaURI FROM dbpediaEntityInTopic " +
						"WHERE topicGroup = '" + topicGroup + "' AND confidence = " + confidence + " AND support = " + support + " AND topicId = " + topicId;
				ResultSet labels = JDBCUtility.executeQuerySingleConnection(sql);
				while(labels.next()) {
					String annotatedText = labels.getString(1);
					if(annotatedText.contains(" ")) {
						title += " #1(" + annotatedText + ") ";
					}
				}
				
				System.out.println(title);
				queryFileOS.write((QueryUtility.checkSpace(title) + "\n").getBytes());
				
				queryFileOS.write("</text>\n".getBytes());
				queryFileOS.write("</query>\n\n".getBytes());
			}
			queryFileOS.write("</parameters>\n".getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Ke:
//		generateProfilesAndQueryFiles(args);
		
		//new:
//		QueryGeneration strategy_dbpedia_multiple = new QueryGeneration_DBpedia_MultipleSources();
//		generateQueryFile(strategy_dbpedia_multiple, "trec_2011_microblog_example", "example");
//		generateQueryFile(strategy_dbpedia_multiple, "trec_2011_microblog", "trec");
		
		//final:
//		QueryGeneration strategy_dbpedia_multiple = new QueryGeneration_DBpedia_MultipleSources();
//		int run = 19;
//		QueryGeneration strategy_dbpedia_multiple_expanded = new QueryGeneration_DBpedia_Expanded_MultipleSources(run);
//		QueryGeneration merge = new QueryGeneration_Merge(strategy_dbpedia_multiple, 0.75, strategy_dbpedia_multiple_expanded, 0.25);
//		
//		generateQueryFile(merge, "trec_2011_microblog", "trec");
//		
//		// news
//		QueryGeneration strategy_dbpedia_multiple = new QueryGeneration_DBpedia_MultipleSources();
//		int run = 20;
//		QueryGeneration strategy_dbpedia_multiple_expanded = new QueryGeneration_DBpedia_Expanded_MultipleSources(run);
//		QueryGeneration strategy_dbpedia_newsdesc_expanded = new QueryGeneration_DBpedia_Expanded_NewsDescription(run);
		// The final configuration is 0.6, 0.3, 0.1
//		QueryGeneration merge = new QueryGeneration_Merge(strategy_dbpedia_newsdesc_expanded, 0.25, strategy_dbpedia_multiple_expanded, 0.75);
//		merge = new QueryGeneration_Merge(merge, 0.4, strategy_dbpedia_multiple, 0.6);
//		generateQueryFile(merge, "trec_2011_microblog", "trec");
		
		// The final configuration for dbpedia expanded run: 0.8 0.2
//		QueryGeneration strategy_dbpedia_multiple_expanded = new QueryGeneration_DBpedia_Expanded_MultipleSources(run);
//		QueryGeneration merge = new QueryGeneration_Merge(strategy_dbpedia_multiple, 0.8, strategy_dbpedia_multiple, 0.6);

//		generateQueryFile(merge, "trec_2011_microblog", "trec");
		
		QueryGeneration_Plain plain_strategy = new QueryGeneration_Plain();
		generateQueryFile(plain_strategy, "trec_2011_microblog", "trec");
		
	}
	
	
	/**
	 * @param args
	 */
	public static void spotlightBased(String[] args) {
		String filename = args[0];
		String filenameAnnotated = args[0] + "-annotated";
		double confidence = Double.parseDouble(args[1]);
		int support = Integer.parseInt(args[2]);
		String topicGroup = args[3];
		
		String sql = "SELECT distinct(topicId) FROM dbpediaEntityInTopic " +
				"WHERE topicGroup = '" + topicGroup + "' AND confidence = " + confidence + " AND support = " + support; 
			
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(sql);
		
		try {
			File queryFile = new File("./query/" + filename);
			File queryFileAnnotated = new File("./query/" + filenameAnnotated);
			FileOutputStream queryFileOS = new FileOutputStream(queryFile);
			FileOutputStream queryFileAnnotatedOS = new FileOutputStream(queryFileAnnotated);
					
			queryFileOS.write("<parameters>\n".getBytes());
			queryFileAnnotatedOS.write("<parameters>\n".getBytes());
			
			while(rs.next()) {
				int topicId = rs.getInt(1);
				
				queryFileOS.write("<query>\n".getBytes());
				queryFileAnnotatedOS.write("<query>\n".getBytes());
				queryFileOS.write(("<type>indri</type>\n").getBytes());
				queryFileAnnotatedOS.write(("<type>indri</type>\n").getBytes());
				queryFileOS.write(("<number>" + topicId + "</number>\n").getBytes());
				queryFileAnnotatedOS.write(("<number>" + topicId + "</number>\n").getBytes());
				queryFileOS.write("<text>\n".getBytes());
				queryFileAnnotatedOS.write("<text>\n".getBytes());
				
				sql = "SELECT annotatedText, dbpediaURI FROM dbpediaEntityInTopic " +
						"WHERE topicGroup = '" + topicGroup + "' AND confidence = " + confidence + " AND support = " + support + " AND topicId = " + topicId;
				ResultSet labels = JDBCUtility.executeQuerySingleConnection(sql);
				while(labels.next()) {
					String annotatedText = labels.getString(1);
					queryFileAnnotatedOS.write((QueryUtility.checkSpace(annotatedText) + "\n").getBytes());
					
					String dbpediaURI = labels.getString(2);
					dbpediaURI = QueryUtility.dbpediaURItoLabel(dbpediaURI);
					queryFileOS.write((QueryUtility.checkSpace(dbpediaURI) + "\n").getBytes());
				}
				
				queryFileOS.write("</text>\n".getBytes());
				queryFileAnnotatedOS.write("</text>\n".getBytes());
				queryFileOS.write("</query>\n\n".getBytes());
				queryFileAnnotatedOS.write("</query>\n\n".getBytes());
			}
//			<query>
//			<type>indri</type>
//			<number>1</number>
//			<text>
//			Chavez
//			</text>
//			</query>
			
			queryFileOS.write("</parameters>\n".getBytes());
			queryFileAnnotatedOS.write("</parameters>\n".getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
