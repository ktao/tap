/**
 * 
 */
package nl.wisdelft.twinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import nl.wisdelft.twinder.io.MongoDBUtility;
import nl.wisdelft.twinder.lucene.Indexer;
import nl.wisdelft.twinder.tal.model.Tweet;
import nl.wisdelft.twinder.tal.source.TwitterStreamSource;
import nl.wisdelft.twinder.utility.HTTPUtility;
import nl.wisdelft.twinder.utility.LanguageClassifier;
import nl.wisdelft.twinder.utility.NERService;
import nl.wisdelft.twinder.utility.OpenCalaisAnnotator;
import nl.wisdelft.twinder.utility.PropertyReader;
import nl.wisdelft.twinder.utility.TweetUtility;

/**
 * Preprocess the tweets, including:
 *  - receiving data from Twitter streaming API, e.g. demo version = tracking news media.
 *  - semantic enrichment
 *  - storage
 *  - indexing
 *  
 * @author ktao
 *
 */
public class PreprocessTREC2011Data {

	private final static Logger logger = LoggerFactory.getLogger(PreprocessTREC2011Data.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/Users/ktao/Desktop/trec2011-judged.json"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Indexer indexer = new Indexer(PropertyReader.getString("tal.index.path"));

		int step = 100;
		logger.info("Every " + step + " tweets will be indexed and stored.");
		
		NERService ner = new OpenCalaisAnnotator();
		HashMap<Long, Tweet> tweetsMap = new HashMap<Long, Tweet>();
		
	    // Do whatever needs to be done with messages
		String msg = null;
	    try {
			while ((msg = br.readLine()) != null) {
				try {
					Tweet t = new Tweet(TwitterObjectFactory.createStatus(msg));
					t.storeOcResult(ner.enrich(t.getText())); // semantics
					if (t.language == null) {
						t.language = LanguageClassifier.getInstance().detect(TweetUtility.preProcessTweet(t.getText()));
					}
					if (!t.language.equals("en")) {
						continue;
					}
					tweetsMap.put(t.getId(), t);	    	
					
					logger.info(tweetsMap.size() + " tweets collected for semantic analysis.");
					
					if (tweetsMap.size() % step == 0) {
						if (tweetsMap.size() == 500) {
							logger.error("Dropped 500 messages due to error.");
							tweetsMap.clear();
							continue;
						}
						String bulk = "{\"data\": [";
						
						for (Long id : tweetsMap.keySet()) {
							bulk += "{\"text\": " + JSONObject.quote(tweetsMap.get(id).getText()) +", "
									+ "\"id\": " + id + "}, ";
						}
						bulk = bulk.substring(0, bulk.length() - 2);
						bulk += "]}";
						try {
							JSONObject json = new JSONObject(bulk);
						} catch (Exception e) {
							System.out.println(bulk);
							System.exit(0);
						}
						// invoke sentiment 140 api
						String jsonResponse = HTTPUtility.invokeRESTful(
								"http://www.sentiment140.com/api/bulkClassifyJson?appid=k.tao.tudelft@gmail.com",
								bulk);
						// update results
						JSONArray sentimentResponse = null;
						try {
							sentimentResponse = new JSONObject(jsonResponse).getJSONArray("data");
						} catch (twitter4j.JSONException e) {
							logger.error("Parsing error from sentiment 140 Web service;");
							logger.error(jsonResponse);
						}
						
						for (int i = 0; i < sentimentResponse.length(); i++) {
							int sentiment = 0;
							long id = -1L;
							try {
								sentiment = sentimentResponse.getJSONObject(i).getInt("polarity");
								id = sentimentResponse.getJSONObject(i).getLong("id");
							} catch (JSONException e) {
								System.err.println("Error " + sentimentResponse.getJSONObject(i));
							}
							tweetsMap.get(id).sentiment = sentiment;
						}

						System.out.println(indexer.indexTweetObjects(tweetsMap) + " tweets indexed.");
						System.out.println(MongoDBUtility.insertTweets(tweetsMap.values()) + " tweets inserted.");
						tweetsMap.clear();
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	    indexer.close();
	}
}
