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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import nl.wisdelft.twinder.io.MongoDBUtility;
import nl.wisdelft.twinder.relevance.SearchWithRelevanceEstimation;
import nl.wisdelft.twinder.utility.JSONManipulator;
import nl.wisdelft.twinder.utility.NERResult;
//import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * This an abstraction of the basic element in Twitter Analysis Language, a.k.a. TAL. 
 * 
 * The original plan was to create an extension of "Interaction class" from DataSift Java client
 * library. However, the manipulation of JSON object seems to be difficult for subsequent processing
 * as well as the further development. Therefore, we decide to create a MAP object for this.
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 22, 2014
 */

public class Tweet implements Status {
	
	private static final long serialVersionUID = 1373616158680285663L;
	/**
	 * The fields customized by DataSift
	 */
	
	private static final SimpleDateFormat dsDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
	
	public static Date parseDSDate(String str) {
		try {
			return dsDateFormatter.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Demographics / gender, possible = mostly_female, mostly_male, female, male, unisex, null */
	private String dsGender;
	
	/**
	 * The gender field
	 * @return gender
	 */
	public String getDSGender() {
		return dsGender;
	}
	
	/** klout score */
	private int dsKloutScore;
	
	public int getDSKloutScore() {
		return dsKloutScore;
	}
	
	/**	language */
	private String dsLangTag;
	private String dsLangTagExtended;
	private int dsLangConfidence;
	
	/**
	 * Get the language detection 
	 * @return
	 */
	public String getDSLang() {
		return dsLangTag;
	}
	
	/** sentiment score given by DataSift */
	private int dsSentiment;
	
	/**
	 * Within DataSift, sentiment is typically scored from -20 to 20 although values outside this 
	 * range do sometimes occur. A score of zero is neutral, scores below zero indicate negative 
	 * sentiment, and scores above zero indicate positive sentiment. For example, a score of -1 is 
	 * mildly negative while a score of 15 is strongly positive.
	 * @return sentiment score
	 */
	public int getDSSentiment() {
		return dsSentiment;
	}
	
	/** sentiment detected by sentiment140 API, 0 = negative, 2 = neutral, 4 = positive */
	public int sentiment;
	
	/** ISO language two-letter code */
	public String language = null;
	
	/**
	 * The fields customized by TAL
	 */
	private SemanticEntity[] wpmEntities;
	private SemanticEntity[] dbpEntities;
	private SemanticEntity[] ocEntities;
	private SemanticTopic[] ocTopics;
	
	public SemanticEntity[] getWpmEntities() {
		return wpmEntities;
	}

	public SemanticEntity[] getDbpEntities() {
		return dbpEntities;
	}

	public SemanticEntity[] getOcEntities() {
		return ocEntities;
	}
	
	public SemanticTopic[] getOcTopics() {
		return ocTopics;
	}
	
	/**
	 * Constructor at abstraction level 1.
	 * @param id the id of the tweet
	 * @param content the content of the tweet
	 */
	public Tweet(Long id, String content) {
		this.id = id;
		this.content = content;
	}
	
	/**
	 * Constructor using the JSON data from DataSift.
	 * 
	 * see {@link http://dev.datasift.com/docs/targets/twitter} for the structure description of the DataSift
	 * output.
	 * 
	 * @param data
	 */
	public Tweet(JsonNode data) {
		// first check whether it is a retweet, then adapt the construction process with a prefix. 
		String tPrefix;
		if (JSONManipulator.isMissingNode(JSONManipulator.getNode("twitter.retweet", data))) {
			tPrefix = "twitter.";
		} else {
			tPrefix = "twitter.retweet.";
		}
		
		this.id = JSONManipulator.getNode(tPrefix + "id", data).asLong();
		this.content = JSONManipulator.getNode(tPrefix + "text", data).asText();
		this.creationTime = parseDSDate(JSONManipulator.getNode("interaction.created_at", data).asText());
		this.dsGender = JSONManipulator.getNode("demographic.gender", data).asText();
		this.dsKloutScore = JSONManipulator.getNode("klout.score", data).asInt();
		this.dsLangConfidence = JSONManipulator.getNode("language.confidence", data).asInt();
		this.dsLangTag = JSONManipulator.getNode("language.tag", data).asText();
		this.dsLangTagExtended = JSONManipulator.getNode("language.tag_extended", data).asText();
		this.dsSentiment = JSONManipulator.getNode("salience.content.sentiment", data).asInt();
		//this.favorited = JSONManipulator.getNode("salience.content.sentiment", data).asInt();
		
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode("twitter.geo", data))) {
			// check if the geo-location exists
			this.geolocation = new GeoLocation(JSONManipulator.getNode("twitter.geo.latitude", data).asDouble(), 
					JSONManipulator.getNode("twitter.geo.longtitude", data).asDouble());
		}
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode(tPrefix + "hashtags", data))) {
			ArrayList<Hashtag> array = new ArrayList<Hashtag>();
			for (JsonNode node : JSONManipulator.getNode(tPrefix + "hashtags", data)) {
				array.add(new Hashtag(node.asText()));
			}
			Hashtag[] hashtags = new Hashtag[array.size()];
			this.hashtags = array.toArray(hashtags);
		}
		// media currently ignored
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode(tPrefix + "mentions", data))) {
			ArrayList<UserMention> array = new ArrayList<UserMention>();
			for (JsonNode node : JSONManipulator.getNode(tPrefix + "mentions", data)) {
				array.add(new UserMention(node.asText()));
			}
			UserMention[] users = new UserMention[array.size()];
			this.mentionedUsers = array.toArray(users);
			
			int counter = 0;
			for (JsonNode node : JSONManipulator.getNode(tPrefix + "mention_ids", data)) {
				users[counter].setId(node.asLong());
				counter ++;
			}
		}
		
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode("twitter.place", data))) { // place exists
			this.place = new nl.wisdelft.twinder.tal.model.Place(JSONManipulator.getNode("twitter.place", data));
		}
		
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode("twitter.in_reply_to_screen_name", data))) { // exists
			this.replyToScreenName = JSONManipulator.getNode("twitter.in_reply_to_screen_name", data).asText();
			this.replyToStatusId = Long.parseLong(JSONManipulator.getNode("twitter.in_reply_to_status_id", data).asText());
			this.replyToUserId = Long.parseLong(JSONManipulator.getNode("twitter.in_reply_to_user_id", data).asText());
		}
		
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode("twitter.retweeted", data))) { // exists
			retweetedTweet = new Tweet(JSONManipulator.getNode("twitter.retweeted", data), true);
		}
		
		this.user = new nl.wisdelft.twinder.tal.model.User(JSONManipulator.getNode(tPrefix + "user", data));
	}
	
	/**
	 * rtdata twitter.retweeted
	 * @param rtdata
	 * @param embeddedRT
	 */
	public Tweet(JsonNode rtdata, boolean embeddedRT) {
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode("geo", rtdata))) {
			// check if the geo-location exists
			this.geolocation = new GeoLocation(JSONManipulator.getNode("geo.latitude", rtdata).asDouble(), 
					JSONManipulator.getNode("geo.longtitude", rtdata).asDouble());
		}
		this.id = JSONManipulator.getNode("id", rtdata).asLong();
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode("in_reply_to_screen_name", rtdata))) { // exists
			this.replyToScreenName = JSONManipulator.getNode("in_reply_to_screen_name", rtdata).asText();
			this.replyToStatusId = Long.parseLong(JSONManipulator.getNode("in_reply_to_status_id", rtdata).asText());
			this.replyToUserId = Long.parseLong(JSONManipulator.getNode("in_reply_to_user_id", rtdata).asText());
		}
		// twitter.retweeted.place.attributes ignored
		if (!JSONManipulator.isMissingNode(JSONManipulator.getNode("place", rtdata))) { // place exists
			this.place = new nl.wisdelft.twinder.tal.model.Place(JSONManipulator.getNode("place", rtdata));
		}
		this.source = JSONManipulator.getNode("source", rtdata).asText();
		this.user = new nl.wisdelft.twinder.tal.model.User(rtdata.path("user"));
	}
	
	// How can we serialize the

	/**
	 * 
	 * @param status
	 */
	public Tweet(Status status) {
		this.id = status.getId();
		this.content = status.getText();
		this.creationTime = status.getCreatedAt();
		this.hashtags = status.getHashtagEntities();
		this.urls = status.getURLEntities();
		this.replyToScreenName = status.getInReplyToScreenName();
		this.replyToStatusId = status.getInReplyToStatusId();
		this.replyToUserId = status.getInReplyToUserId();
		this.user = new nl.wisdelft.twinder.tal.model.User(status.getUser());
		this.language = status.getLang();
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JsonParseException 
	 */
	public static void main(String[] args) throws JsonParseException, IOException {
		System.out.println("This is the main method in the class of Tweet.");
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
		System.out.println(dsJSONTweet); // done checked
		
//		String testJSON = "{\"name\": \"David\", \"role\": \"Manager\", \"city\": \"Los Angeles\"}";
		
		ObjectMapper mapper = new ObjectMapper();
//		JsonNode actualObj = mapper.readTree(testJSON);
		JsonNode tweet = mapper.readTree(dsJSONTweet);
		
		Tweet t = new Tweet(tweet);
		System.out.println(t.getId());
		System.out.println(t.getText());
		
//		JsonNode nameNode = actualObj.path("name");
//		System.out.println(nameNode.asText());
//		JsonNode gender = tweet.path("demographic").path("gender");
//		System.out.println(gender.asText());
	}

	/**
	 * The order between tweets are defined by their creation time. Should be as same as the order 
	 * of their ids.
	 * @param o The status to be compared to.
	 * @return The comparison result.
	 */
	public int compareTo(Status o) {
		return this.getCreatedAt().compareTo(o.getCreatedAt());
	}

	/**
	 * The access level? 
	 */
	public int getAccessLevel() { // TODO not implemented
		return 0;
	}

	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	/** The hashtags that are mentioned in the tweet */
	private HashtagEntity[] hashtags = null;
	
	public HashtagEntity[] getHashtagEntities() {
		return hashtags;
	}
	
	public BasicDBList getHashtagAsDBList() {
		BasicDBList list = new BasicDBList();
		for (HashtagEntity e : hashtags) {
			list.add(e.getText());
		}
		return list;
	}

	/** The pictures / videos that are mentioned in the tweet */
	private MediaEntity[] media = null;
	
	public MediaEntity[] getMediaEntities() {
		return media;
	}
	
	public BasicDBList getMediaAsDBList() {
		BasicDBList list = new BasicDBList();
		for (MediaEntity e : media) {
//			TODO list.add();
		}
		return list;
	}

	/** The URLs that are mentioned in the tweet */
	private URLEntity[] urls = null;
	
	public URLEntity[] getURLEntities() {
		return urls;
	}
	
	public BasicDBList getURLsAsDBList() {
		BasicDBList list = new BasicDBList();
		for (URLEntity e : urls) {
			BasicDBObject object = new BasicDBObject("url", e.getURL());
			object.append("expandedURL", e.getExpandedURL());
			list.add(object);
		}
		return list;
	}

	/** The users that are mentioned in the tweet */
	private UserMentionEntity[] mentionedUsers;
	
	public UserMentionEntity[] getUserMentionEntities() {
		return mentionedUsers;
	}

	public long[] getContributors() { // TODO not implemented
		// TODO Auto-generated method stub
		return null;
	}

	/** When the tweet was posted */
	private Date creationTime;
	
	public Date getCreatedAt() { // TODO not implemented
		return creationTime;
	}

	/** The geo-location of the tweet */
	private GeoLocation geolocation;
	
	public GeoLocation getGeoLocation() {
		return geolocation;
	}

	/** The unique identifier of the tweet */
	private long id;
	
	public long getId() {
		return id;
	}

	/** This tweet is a reply to some tweet authored by a user with this screen name*/
	private String replyToScreenName = null;
	
	public String getInReplyToScreenName() {
		return replyToScreenName;
	}

	/** This tweet is a reply to the tweet with this id */
	private long replyToStatusId = -1;
	
	public long getInReplyToStatusId() {
		return replyToStatusId;
	}

	/** This tweet is a reply to the tweet authored by the user of this id */
	private long replyToUserId = -1;
	
	public long getInReplyToUserId() {
		return replyToUserId;
	}

	/** The place entity tagged to this tweet */
	private Place place;
	
	public Place getPlace() {
		return place;
	}

	/** Returns the number of times this tweet has been retweeted, or -1 when the tweet was created before this feature was enabled. */
	private int retweetCount = -1;
	
	public int getRetweetCount() {
		return retweetCount;
	}

	/** The original tweet that was retweeted from */
	private Tweet retweetedTweet;
	
	public Tweet getRetweetedStatus() {
		return retweetedTweet;
	}

	/** Where is this tweet posted? or the used device / application / website */
	private String source;
	
	public String getSource() {
		return source;
	}

	/** The content of the tweet */
	private String content;
	
	public String getText() {
		return content;
	}

	/** The author of this Tweet*/
	private nl.wisdelft.twinder.tal.model.User user;
	
	public User getUser() {
		if (user != null)
			return user;
		else {
			System.err.println("The author of tweet " + id + " is undefined.");
			return null;
		}
	}

	/** Whether this tweet has been marked as favorited? */
	private boolean favorited;
	///** If so, how many times? */
	//private int favCounts;
	
	public boolean isFavorited() {
		return favorited;
	}
	
	/** If this tweet has been retweeted? */
	private boolean retweeted;
	
	public boolean isRetweet() {
		return retweeted;
	}

	public boolean isRetweetedByMe() {
		return false;
	}

	/** Whether this status is truncated? */
	private boolean truncated;
	
	public boolean isTruncated() {
		return truncated;
	}

	/**
	 * Store the OpenCalais results (=topics and entities)
	 * @param result
	 */
	public void storeOcResult(NERResult result) {
		if (result == null) {
			System.err.println("Nothing to store for tweet " + this.getId() + ":(");
			return;
		}
			
		if (result.getEntities() != null)
			this.ocEntities = result.getEntities().toArray(new SemanticEntity[0]);
		if (result.getTopics() != null)
			this.ocTopics = result.getTopics().toArray(new SemanticTopic[0]);
	}
	
	public static final int MONGODB_STORE_LEVEL_1 = 1;
	public static final int MONGODB_STORE_LEVEL_2 = 2;
	
	public BasicDBObject toBasicDBObject(int level) {
		BasicDBObject doc = new BasicDBObject("id", this.id);
		switch (level) {
			case MONGODB_STORE_LEVEL_1: // id + content
				doc.append("content", this.content);
				return doc;
			case MONGODB_STORE_LEVEL_2: // for Twinder - relevance
				doc.append("content", this.content);
				doc.append("created_at", this.creationTime);
				if (this.hashtags != null)
					doc.append("hashtags", this.getHashtagAsDBList());
				if (this.urls != null)
					doc.append("urls", this.getURLsAsDBList());
				if (this.replyToScreenName != null)
					doc.append("in_reply_to_screen_name", this.replyToScreenName);
				if (this.replyToStatusId != -1)
					doc.append("in_reply_to_status_id", this.replyToStatusId);
				if (this.replyToUserId != -1)
					doc.append("in_reply_to_user_id", this.replyToUserId);
				doc.append("user", this.user.toBasicDBObject(level));
				doc.append("lang", this.language);
				doc.append("sentiment", this.sentiment);
				if (this.ocEntities != null)
					doc.append("semanticEntities", MongoDBUtility.convertArray(this.ocEntities));
				if (this.ocTopics != null)
					doc.append("semanticTopics", MongoDBUtility.convertArray(this.ocTopics));
				return doc;
			default:
				return doc;
		}
	}

	public SymbolEntity[] getSymbolEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getCurrentUserRetweetId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getFavoriteCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getLang() {
		// TODO Auto-generated method stub
		return null;
	}

	public Scopes getScopes() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPossiblySensitive() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRetweeted() {
		// TODO Auto-generated method stub
		return false;
	}
}
