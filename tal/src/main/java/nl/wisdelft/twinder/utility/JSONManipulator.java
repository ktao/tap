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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 22, 2014
 */
public class JSONManipulator {
	
	ObjectMapper mapper;
	JsonNode root;
	
	public JSONManipulator() {
		mapper = new ObjectMapper();
	}
	
	public void load(String jsonString) {
		try {
			root = mapper.readTree(jsonString);
		} catch (JsonProcessingException e) {
			root = null;
			e.printStackTrace();
		} catch (IOException e) {
			root = null;
			e.printStackTrace();
		}
	}
	
	public JsonNode getNode(String paths) throws Exception {
		return getNode(paths, root);
	}
	
	public static JsonNode getNode(String paths, JsonNode node) {
		if (paths.contains(".")) {
//			System.out.println(paths);
//			System.out.println(paths.indexOf('.'));
			return getNode(paths.substring(paths.indexOf('.') + 1), 
					node.path(paths.substring(0, paths.indexOf('.'))));
		} else {
			return node.path(paths);
		}
			
	}
	
	public static boolean isMissingNode(JsonNode node) {
		return node.getClass() == MissingNode.class;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String dsJSONTweet = "{\"demographic\":{\"gender\":\"unisex\"},\"interaction\":{\"schema\":" +
				"{\"version\":3},\"source\":\"Twitter for iPhone\",\"author\":{\"username\":\"danko" +
				"day\",\"name\":\"Dan Koday\",\"id\":14203603,\"avatar\":\"http://pbs.twimg.com/" +
				"profile_images/457895175990173696/uuAaHWZw_normal.png\",\"link\":\"http://twitter." +
				"com/dankoday\",\"language\":\"en\"},\"type\":\"twitter\",\"created_at\":\"Tue, 22 " +
				"Apr 2014 16:00:31 +0000\",\"received_at\":1.3981824313809E9,\"content\":\"That mo" +
				"ment you track something on @WeightWatchers and it's 17 points instead of then 8 " +
				"you thought it would be. #wwfail #fireislanddiet\",\"id\":\"1e3ca373ad07a980e074e" +
				"84f55141086\",\"link\":\"http://twitter.com/dankoday/status/458636474523844608\"," +
				"\"geo\":{\"latitude\":40.76438197,\"longitude\":-73.97704157},\"mentions\":[\"Weig" +
				"htWatchers\"],\"mention_ids\":[63488455],\"hashtags\":[\"wwfail\",\"fireislanddie" +
				"t\"]},\"klout\":{\"score\":48},\"language\":{\"tag\":\"en\",\"tag_extended\":\"en" +
				"\",\"confidence\":98},\"salience\":{\"content\":{\"sentiment\":0}},\"twitter\":{\"" +
				"created_at\":\"Tue, 22 Apr 2014 16:00:31 +0000\",\"filter_level\":\"medium\",\"geo" +
				"\":{\"latitude\":40.76438197,\"longitude\":-73.97704157},\"hashtags\":[\"wwfail\"," +
				"\"fireislanddiet\"],\"id\":\"458636474523844608\",\"lang\":\"en\",\"mention_ids\":" +
				"[63488455],\"mentions\":[\"WeightWatchers\"],\"place\":{\"id\":\"27485069891a7938\"" +
				",\"url\":\"https://api.twitter.com/1.1/geo/id/27485069891a7938.json\",\"place_type" +
				"\":\"city\",\"country\":\"United States\",\"country_code\":\"US\",\"full_name\":" +
				"\"New York, NY\",\"name\":\"New York\",\"attributes\":{}},\"source\":\"<a href=\\" +
				"\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone" +
				"</a>\",\"text\":\"That moment you track something on @WeightWatchers and it's 17 " +
				"points instead of then 8 you thought it would be. #wwfail #fireislanddiet\",\"user" +
				"\":{\"name\":\"Dan Koday\",\"url\":\"http://www.dankoday.com\",\"description\":\"" +
				"Executive Content Director @Latina | Style, entertainment & lifestyle editor | " +
				"Interests: Travel, Food, Entertainment & Style | Follow on Instagram: @dankoday" +
				"\",\"location\":\"New York, NY\",\"statuses_count\":3799,\"followers_count\":940," +
				"\"friends_count\":461,\"screen_name\":\"dankoday\",\"profile_image_url\":\"http://" +
				"pbs.twimg.com/profile_images/457895175990173696/uuAaHWZw_normal.png\",\"profile_" +
				"image_url_https\":\"https://pbs.twimg.com/profile_images/457895175990173696/uuAa" +
				"HWZw_normal.png\",\"lang\":\"en\",\"time_zone\":\"Eastern Time (US & Canada)\",\"" +
				"utc_offset\":-14400,\"listed_count\":29,\"id\":14203603,\"id_str\":\"14203603\",\"" +
				"geo_enabled\":true,\"verified\":false,\"favourites_count\":7,\"created_at\":\"Sun, " +
				"23 Mar 2008 20:52:25 +0000\"}}}";
		System.out.println(dsJSONTweet);
		
		String testJSON = "{\"name\": \"David\", \"role\": \"Manager\", \"city\": \"Los Angeles\"}";
		
		JSONManipulator manipulator = new JSONManipulator();
		manipulator.load(dsJSONTweet);
		
		
		System.out.println(manipulator.getNode("twitter.id").asText());
		
		System.out.println(manipulator.getNode("interaction.created_at").asText());
//		System.out.println(manipulator.getNode("interaction.created_at").getClass());
		System.out.println(manipulator.getNode("interaction.geolocation"));
		
		for (JsonNode node : manipulator.getNode("twitter.hashtags")) {
			System.out.println(node.asText());
		}
		
//		JsonNode actualObj = mapper.readTree(testJSON);
//		JsonNode tweet = mapper.readTree(dsJSONTweet);
		
//		JsonNode nameNode = actualObj.path("name");
		
//		System.out.println(nameNode.asText());
		
//		JsonNode gender = tweet.path("demographic").path("gender");
		
//		System.out.println(gender.asText());
	}

}
