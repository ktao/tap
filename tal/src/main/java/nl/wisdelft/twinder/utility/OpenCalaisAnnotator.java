/*********************************************************
*  Copyright (c) 2013 by Web Information Systems (WIS) Group.
*  Ke Tao, http://ktao.nl/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.wisdelft.twinder.tal.model.SemanticEntity;
import nl.wisdelft.twinder.tal.model.SemanticTopic;

import mx.bigdata.jcalais.CalaisClient;
import mx.bigdata.jcalais.CalaisObject;
import mx.bigdata.jcalais.CalaisResponse;
import mx.bigdata.jcalais.rest.CalaisRestClient;

/**
 * Annotate the messages with OpenCalais Named-Entity Recognition service. 
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 24, 2014
 */
public class OpenCalaisAnnotator implements NERService {
	/** The API key that is being used */
	public static String API_KEY;
	
	private static List<String> API_KEYS = new ArrayList<String>();
	static {
		for (int i = 1; i <= PropertyReader.getInt("tal.apikey.opencalais.number"); i++) {
			API_KEYS.add(PropertyReader.getString("tal.apikey.opencalais." + i));
		}
		API_KEY = API_KEYS.get(0);
	}
	
	private static void switchAPIKey(){
		int index = API_KEYS.indexOf(API_KEY) + 1;
		if(index < API_KEYS.size()){
			API_KEY = API_KEYS.get(index);
		}else{
			API_KEY = API_KEYS.get(0);
		}
	}
	
	public static String switchAPIKey(String oldKey){
		int index = API_KEYS.indexOf(oldKey) + 1;
		if(index < API_KEYS.size() && index > 0){
			return API_KEYS.get(index);
		}else{
			return API_KEYS.get(0);
		}
	}
	
	/** The client object to invoke the OpenCalais service */
	private CalaisClient client;
	
	public OpenCalaisAnnotator() {
		client = new CalaisRestClient(API_KEY);
	}

	/**
	 * Testing code
	 * TODO move this to JUnitTest
	 */
	public static void testOpenCalais() {
		CalaisClient client = new CalaisRestClient(API_KEY);
	    CalaisResponse response = null;
	    
	    try {
			response = client.analyze("Delft University is actually Delft University of Technology in the Netherlands.");
			if(response.getEntities() != null){
				for (CalaisObject entity : response.getEntities()) {
					System.out.println(entity);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public NERResult enrich(String text) {
		text = TweetUtility.preProcessTweet(text);
		try {
			CalaisResponse response = client.analyze(text);
			NERResult result = new NERResult();
			if(response.getEntities() != null){
				for (CalaisObject entity : response.getEntities()) {
					result.addEntity(new SemanticEntity(
							"UNDEFINED", 
							entity.getField("name"), 
							entity.getField("_uri"), 
							entity.getField("_type"),
							entity.getField("_typeReference"),
							Double.parseDouble(entity.getField("relevance"))));
				}
			}
			if(response.getTopics() != null) {
				for (CalaisObject topic : response.getTopics()) {
					result.addTopic(new SemanticTopic(
							topic.getField("categoryName"), 
							topic.getField("category"),
							Double.parseDouble(topic.getField("score"))));
				}
			}
			
			return result;
		} catch (IOException e) {
			switchAPIKey();
			client = new CalaisRestClient(API_KEY);
			//e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (java.lang.NullPointerException e) {
			return null;
		}
	}
	
	public void test(String text) {
		try {
			CalaisResponse response = client.analyze(text);
			System.out.println(response.getEntities());
			System.out.println(response.getTopics()); 
			System.out.println(response.getRelations());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OpenCalaisAnnotator anno = new OpenCalaisAnnotator();
		anno.test("POWER BIEBER sucks");
	}
}
