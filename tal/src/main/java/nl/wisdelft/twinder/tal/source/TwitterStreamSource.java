/**
 * 
 */
package nl.wisdelft.twinder.tal.source;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import nl.wisdelft.twinder.utility.PropertyReader;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.OAuth2Authorization;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author ktao
 *
 */
public class TwitterStreamSource {
	
	BlockingQueue<String> queue;
	StatusesFilterEndpoint endpoint;
//	StatusesSampleEndpoint endpoint;
	Authentication auth;
	Client client;
	
	public TwitterStreamSource() {
		this.auth = new OAuth1(PropertyReader.getString("tal.twitter.api.consumerkey"), 
	    		PropertyReader.getString("tal.twitter.api.consumersecret"), 
	    		PropertyReader.getString("tal.twitter.api.token"), 
	    		PropertyReader.getString("tal.twitter.api.tokensecret"));
		
		this.queue = new LinkedBlockingQueue<String>(10000);
	    this.endpoint = new StatusesFilterEndpoint();
//		this.endpoint = new StatusesSampleEndpoint();
	    
	    // add tracking followers
	    List<Long> following = new ArrayList<Long>();
	    following = readFollowingFromFile(PropertyReader.getString("tal.resources.source.following"), following);
	    endpoint.followings(following);

	    // Create a new BasicClient. By default gzip is enabled.
	    client = new ClientBuilder()
	      .hosts(Constants.STREAM_HOST)
	      .endpoint(endpoint)
	      .authentication(auth)
	      .processor(new StringDelimitedProcessor(queue))
	      .build();
	}
	
	public void connect() {
		this.client.connect();
	}
	
	public boolean isDone() {
		return this.client.isDone();
	}
	
	public String take() {
		if (this.client.isDone()) {
			return null;
		} else {
			try {
				return queue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public void stop() {
		client.stop();
	}
	
	public static List<Long> readFollowingFromFile(String filename, List<Long> following) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = br.readLine()) != null) {
				following.add(Long.parseLong(line.trim()));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return following;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TwitterStreamSource tss = new TwitterStreamSource();

		tss.connect();
		
	    // Do whatever needs to be done with messages
	    while (!tss.isDone()) {
	    	String msg = tss.take();
	    	
	    	System.out.println(msg);
	    }

	    tss.stop();
	}

	/**
	 * Consume the Twitter stream with Twitter4j
	 */
	@Deprecated
	public static void consumeWithTwitter4j() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(PropertyReader.getString("tal.twitter.api.consumerkey"))
			.setOAuthConsumerSecret(PropertyReader.getString("tal.twitter.api.consumersecret"))
			.setOAuthAccessToken(PropertyReader.getString("tal.twitter.api.token"))
			.setOAuthAccessTokenSecret(PropertyReader.getString("tal.twitter.api.tokensecret"));
		OAuth2Authorization auth = new OAuth2Authorization(cb.build());
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance(auth);
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
	}
}
