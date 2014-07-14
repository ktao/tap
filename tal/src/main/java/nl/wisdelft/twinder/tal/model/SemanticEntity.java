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

import nl.wisdelft.twinder.io.DBObjectCovertable;

import com.mongodb.BasicDBObject;

import twitter4j.JSONObject;

/**
 * The semantic entity that may be recognized by Named-Recognition Services.
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 23, 2014
 */
public class SemanticEntity implements DBObjectCovertable {

	private String annotatedText;
	private String name;
	private String URI;
	private String type;
	private String typeURI;
	private double score;
	private int support;
	
	/**
	 */
	public SemanticEntity(String annotatedText, String name, String URI, String type, String typeURI, double score) {
		this.annotatedText = annotatedText;
		this.name = name;
		this.URI = URI;
		this.type = type;
		this.typeURI = typeURI;
		this.score = score;
	}
	
	/** modify when used by others.
	 */
	public SemanticEntity(String annotatedText, String name, String URI, double score, int support) {
		this.annotatedText = annotatedText;
		this.name = name;
		this.URI = URI;
		this.score = score;
		this.support = support;
	}
	
	/**
	 * For testing purpose
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getAnnotatedText() {
		return annotatedText;
	}

	public String getName() {
		return name;
	}

	public String getURI() {
		return URI;
	}
	
	public String getType() {
		return type;
	}

	public String getTypeURI() {
		return typeURI;
	}

	public double getScore() {
		return score;
	}

	public int getSupport() {
		return support;
	}

	public String toString() {
		return "{\"name\" : " + JSONObject.quote(name) + ", "
				+ "\"uri\" : " + JSONObject.quote(URI) + ","
				+ "\"type\" : " + JSONObject.quote(type) + ","
				+ "\"score\" : " + score + ","
				+ "\"text\" : " + JSONObject.quote(annotatedText) + "}";
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject object = new BasicDBObject();
		object.append("name", name);
		object.append("uri", URI);
		object.append("type", type);
		object.append("score", score);
		if (annotatedText != null && !annotatedText.equals("UNDEFINED"))
			object.append("text", annotatedText);
		return object;
	}
}
