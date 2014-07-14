/*********************************************************
*  Copyright (c) 2013 by Web Information Systems (WIS) Group.
*  Ke Tao, http://ktao.nl/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.tal.model;

import com.mongodb.BasicDBObject;

import nl.wisdelft.twinder.io.DBObjectCovertable;
import twitter4j.JSONObject;

/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 24, 2014
 */
public class SemanticTopic implements DBObjectCovertable {
	
	/** The name of the topic */
	private String topicName;

	/** The URI of the topic */
	private String topicURI;
	
	/** The confidence / relevance score for this recognization */
	private double score;
	
	public String getTopicName() {
		return topicName;
	}

	public String getTopicURI() {
		return topicURI;
	}

	public double getScore() {
		return score;
	}
	
	/**
	 * constructor
	 * @param topicName
	 * @param topicURI
	 * @param score
	 */
	public SemanticTopic(String topicName, String topicURI, double score) {
		this.topicName = topicName;
		this.topicURI = topicURI;
		this.score = score;
	}
	
	public String toString() {
		return "{\"name\" : " + JSONObject.quote(topicName) + ", "
				+ "\"uri\" : " + JSONObject.quote(topicURI) + ","
				+ "\"score\" : " + score + "}";
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject object = new BasicDBObject();
		object.append("name", topicName);
		object.append("uri", topicURI);
		object.append("score", score);
		return object;
	}
	
	
}
