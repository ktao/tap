/**
 * 
 */
package nl.wisdelft.twinder.tal.model;

import java.net.URL;
import java.util.Date;

import nl.wisdelft.twinder.utility.JSONManipulator;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * @author ktao
 *
 */
public class User implements twitter4j.User {

	/**
	 * 
	 */
	private static final long serialVersionUID = -667524846074130494L;
	/**
	 * twitter.retweeted.user.description	string
	 * twitter.retweeted.user.followers_count	 int	
	 * twitter.retweeted.user.follower_ratio	 float	
	 * twitter.retweeted.user.friends_count	 int	
	 * twitter.retweeted.user.id	 int	
	 * twitter.retweeted.user.lang	 string	
	 * twitter.retweeted.user.listed_count	 int	
	 * twitter.retweeted.user.location	 string	
	 * twitter.retweeted.user.name	 string	
	 * twitter.retweeted.user.profile_age	 int	
	 * twitter.retweeted.user.screen_name	 string	
	 * twitter.retweeted.user.statuses_count	 int	
	 * twitter.retweeted.user.time_zone	 string	
	 * twitter.retweeted.user.url	 string	
	 * twitter.retweeted.user.verified
	 * 
	 */
	
	private String description;
	private int followersCount;
	private double followerRatio; // TODO to be implemented
	private int friendsCount;
	private long id;
	private String lang;
	private int listedCount;
	private String location;
	private String name;
	private int profileAge; // TODO to be implemented
	private String screenName;
	private int statusesCount;
	private String timeZone;
	private String url;
	private boolean verified;
	private String profileImageURL;
	private Date createdAt;
	
	public User(JsonNode data) {
		this.description = JSONManipulator.getNode("description", data).asText();
		this.followersCount = JSONManipulator.getNode("followers_count", data).asInt();
		this.followerRatio = JSONManipulator.getNode("follower_ratio", data).asDouble();
		this.friendsCount = JSONManipulator.getNode("friends_count", data).asInt();
		this.id = JSONManipulator.getNode("id", data).asLong();
		this.lang = JSONManipulator.getNode("lang", data).asText();
		this.listedCount = JSONManipulator.getNode("listed_count", data).asInt();
		this.location = JSONManipulator.getNode("location", data).asText();
		this.name = JSONManipulator.getNode("name", data).asText();
		this.profileAge = JSONManipulator.getNode("profile_age", data).asInt();
		this.screenName = JSONManipulator.getNode("screen_name", data).asText();
		this.statusesCount = JSONManipulator.getNode("statuses_count", data).asInt();
		this.timeZone = JSONManipulator.getNode("time_zone", data).asText();
		this.url = JSONManipulator.getNode("url", data).asText();
		this.verified = JSONManipulator.getNode("verified", data).asBoolean();
	}
	
	/**
	 * 
	 * @param user
	 */
	public User(twitter4j.User user) {
		this.description = user.getDescription();
//		this.followerRatio = user.get;
		this.followersCount = user.getFollowersCount();
		this.friendsCount = user.getFriendsCount();
		this.id = user.getId();
		this.lang = user.getLang();
		this.listedCount = user.getListedCount();
		this.location = user.getLocation();
		this.name = user.getName();
		this.screenName = user.getScreenName();
		this.statusesCount = user.getStatusesCount();
		this.timeZone = user.getTimeZone();
		this.url = user.getURL();
		this.verified = user.isVerified();
		this.profileImageURL = user.getProfileImageURL();
		this.createdAt = user.getCreatedAt();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(twitter4j.User o) { // TODO not implemented
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.TwitterResponse#getAccessLevel()
	 */
	public int getAccessLevel() { // TODO not implemented
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.TwitterResponse#getRateLimitStatus()
	 */
	public RateLimitStatus getRateLimitStatus() { // TODO not implemented
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getCreatedAt()
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getDescription()
	 */
	public String getDescription() {
		return this.description;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getFavouritesCount()
	 */
	public int getFavouritesCount() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getFollowersCount()
	 */
	public int getFollowersCount() {
		return this.followersCount;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getFriendsCount()
	 */
	public int getFriendsCount() {
		return this.friendsCount;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getId()
	 */
	public long getId() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getLang()
	 */
	public String getLang() {
		return this.lang;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getListedCount()
	 */
	public int getListedCount() {
		return this.listedCount;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getLocation()
	 */
	public String getLocation() {
		return this.location;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileBackgroundColor()
	 */
	public String getProfileBackgroundColor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileBackgroundImageUrl()
	 */
	public String getProfileBackgroundImageUrl() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileBackgroundImageUrlHttps()
	 */
	public String getProfileBackgroundImageUrlHttps() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileImageUrlHttps()
	 */
	public URL getProfileImageUrlHttps() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileLinkColor()
	 */
	public String getProfileLinkColor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileSidebarBorderColor()
	 */
	public String getProfileSidebarBorderColor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileSidebarFillColor()
	 */
	public String getProfileSidebarFillColor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getProfileTextColor()
	 */
	public String getProfileTextColor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getScreenName()
	 */
	public String getScreenName() {
		return this.screenName;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getStatus()
	 */
	public Status getStatus() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getStatusesCount()
	 */
	public int getStatusesCount() {
		return this.statusesCount;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getTimeZone()
	 */
	public String getTimeZone() {
		return this.timeZone;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#getUtcOffset()
	 */
	public int getUtcOffset() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isContributorsEnabled()
	 */
	public boolean isContributorsEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isFollowRequestSent()
	 */
	public boolean isFollowRequestSent() {
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isGeoEnabled()
	 */
	public boolean isGeoEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isProfileBackgroundTiled()
	 */
	public boolean isProfileBackgroundTiled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isProfileUseBackgroundImage()
	 */
	public boolean isProfileUseBackgroundImage() {
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isProtected()
	 */
	public boolean isProtected() {
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isShowAllInlineMedia()
	 */
	public boolean isShowAllInlineMedia() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isTranslator()
	 */
	public boolean isTranslator() {
		return false;
	}

	/* (non-Javadoc)
	 * @see twitter4j.User#isVerified()
	 */
	public boolean isVerified() {
		return verified;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getBiggerProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBiggerProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public URLEntity[] getDescriptionURLEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMiniProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMiniProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOriginalProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOriginalProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBackgroundImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerIPadRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerIPadURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerMobileRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerMobileURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileImageURL() {
		return profileImageURL;
	}

	public String getProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public URLEntity getURLEntity() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public BasicDBObject toBasicDBObject(int level) {
		BasicDBObject doc = new BasicDBObject("id", this.id);
		doc.append("name", this.name);
		doc.append("screenName", this.screenName);
		switch (level) {
			case Tweet.MONGODB_STORE_LEVEL_1:
				doc.append("profile_image_url", this.getProfileImageURL());
				return doc;
			case Tweet.MONGODB_STORE_LEVEL_2: // for Twinder - relevance
				doc.append("profile_image_url", this.getProfileImageURL());
				doc.append("followers_count", this.followersCount);
				doc.append("listed_count", this.listedCount);
				doc.append("created_at", this.getCreatedAt());
				doc.append("statuses_count", this.statusesCount);
				return doc;
			default:
				return doc;
		}
	}
}
