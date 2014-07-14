/**
 * 
 */
package nl.wisdelft.twinder.tal.model;

import com.fasterxml.jackson.databind.JsonNode;

import twitter4j.GeoLocation;
import twitter4j.RateLimitStatus;

/**
 * @author ktao
 *
 */
public class Place implements twitter4j.Place {

	/* prefix twitter.place
	 * "id":"27485069891a7938",
	 * "url":"https://api.twitter.com/1.1/geo/id/27485069891a7938.json",
	 * "place_type":"city",
	 * "country":"United States",
	 * "country_code":"US",
	 * "full_name":"New York, NY",
	 * "name":"New York",
	 * "attributes":{ currently ignored
	 * 		twitter.place.attributes.locality	 array(string)	
	 *  	twitter.place.attributes.region	 array(string)	
	 *  	twitter.place.attributes.street_address
	 * }
	 * 
	 * 
	 */
	private String id;
	private String url;
	private String placeType;
	private String country;
	private String countryCode;
	private String fullName;
	private String name;
	
	/**
	 * default constructor
	 */
	public Place() {
		
	}
	
	/**
	 * Using a JsonNode to initialize the Place
	 * @param data
	 */
	public Place(JsonNode data) {
		this.id = data.path("id").asText();
		this.placeType = data.path("place_type").asText();
		this.country = data.path("country").asText();
		this.countryCode = data.path("country_code").asText();
		this.fullName = data.path("full_name").asText();
		this.name = data.path("name").asText();
	}
	
	
	/* (non-Javadoc)
	 * @see twitter4j.TwitterResponse#getAccessLevel()
	 */
	public int getAccessLevel() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.TwitterResponse#getRateLimitStatus()
	 */
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(twitter4j.Place o) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getBoundingBoxCoordinates()
	 */
	public GeoLocation[][] getBoundingBoxCoordinates() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getBoundingBoxType()
	 */
	public String getBoundingBoxType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getContainedWithIn()
	 */
	public twitter4j.Place[] getContainedWithIn() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getCountry()
	 */
	public String getCountry() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getCountryCode()
	 */
	public String getCountryCode() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getFullName()
	 */
	public String getFullName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getGeometryCoordinates()
	 */
	public GeoLocation[][] getGeometryCoordinates() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getGeometryType()
	 */
	public String getGeometryType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getId()
	 */
	public String getId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getName()
	 */
	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getPlaceType()
	 */
	public String getPlaceType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getStreetAddress()
	 */
	public String getStreetAddress() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.Place#getURL()
	 */
	public String getURL() {
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
