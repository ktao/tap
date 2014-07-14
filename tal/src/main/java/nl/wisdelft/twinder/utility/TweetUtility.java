/**
 * 
 */
package nl.wisdelft.twinder.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ktao
 *
 */
public class TweetUtility {

	/** regular expression for URLs */
	private static final String URIREGEX = 
			"https?://([-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|])"; 
	
	public static String preProcessTweet(String tweet) {
		tweet = tweet.replaceAll("@([A-Za-z0-9_]+)", "");
		Pattern pattern = Pattern.compile("(\\w)\\1{3,}");
		Matcher matcher = pattern.matcher(tweet);
		while(matcher.find()) {
			tweet = tweet.replaceFirst("(\\w)\\1{3,}", matcher.group().substring(0, 1));
		}
		tweet = tweet.replaceAll(URIREGEX, "");
		
		return tweet;
	}
	
	public static String removeURLs(String content) {
		return content.replaceAll(URIREGEX, "");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
