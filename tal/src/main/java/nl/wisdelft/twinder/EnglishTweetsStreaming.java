/**
 * 
 */
package nl.wisdelft.twinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wisdelft.twinder.tal.model.Tweet;
import nl.wisdelft.twinder.tal.source.TwitterStreamSource;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 * The objective code to which a script in TAL should be translated.
 * 
 * The function described here is to get the streaming sample from Twitter and get only the English
 * tweets. However, nothing will be stored or output.
 * 
 * @author ktao
 */
public class EnglishTweetsStreaming {
	
	private final static Logger logger = LoggerFactory.getLogger(EnglishTweetsStreaming.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TwitterStreamSource tss = new TwitterStreamSource();

		tss.connect();
		
	    // Do whatever needs to be done with messages
	    while (!tss.isDone()) {
	    	String msg = tss.take();
	    	try {
	    		Tweet t = new Tweet(TwitterObjectFactory.createStatus(msg));
	    		
	    		if (!t.language.equals("en")) {
	    			continue;
	    		}
	    		
	    	} catch (TwitterException e) {
	    		e.printStackTrace();
	    	} catch (Exception e) {
	    		logger.error("Interrupted.");
				e.printStackTrace();
			}
	    }
	    tss.stop();
	}

}
