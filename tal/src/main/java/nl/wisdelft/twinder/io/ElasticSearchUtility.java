/**
 * 
 */
package nl.wisdelft.twinder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jzlib.GZIPInputStream;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 * 
 * @author ktao
 * TODO: configuration via configuration file.
 */
public class ElasticSearchUtility {
	
	private final static Logger logger = LoggerFactory.getLogger(ElasticSearchUtility.class);
	
	private Client client;
	private String index;
	private String type;
	private ArrayList<String> bulk;
	private static int bulksize = 10000;
	
	/**
	 * The default initializer, with index name "twitter" and type name "tweet"
	 */
	public ElasticSearchUtility() {
		client = new TransportClient()
		.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		bulk = new ArrayList<String>();
		index = "twitter";
		type = "tweet";
	}
	
	public ElasticSearchUtility(String _index, String _type) {
		client = new TransportClient()
		.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		bulk = new ArrayList<String>();
		index = _index;
		type = _type;
	}
	
	public ElasticSearchUtility(String _index, String _type, int _bulksize) {
		client = new TransportClient()
		.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		bulksize = _bulksize;
		bulk = new ArrayList<String>();
		index = _index;
		type = _type;
	}
	
	public void close() {
		client.close();
	}

	/**
	 * index the document with id
	 * @param json
	 * @param id
	 */
	@Deprecated
	public void indexDoc(String json, long id) {
		IndexResponse response = client.prepareIndex(index, type, Long.toString(id))
		        .setSource(json)
		        .execute()
		        .actionGet();
		
		// Index name
		String _index = response.getIndex();
		// Type name
		String _type = response.getType();
		// Document ID (generated or not)
		String _id = response.getId();
		// Version (if it's the first time you index this document, you will get: 1)
		long _version = response.getVersion();
		
		logger.info("Indexd document, id = " + _id + " at"
				+ " index: " + _index 
				+ " type: " + _type
				+ " version: " + _version);
	}
	
	/**
	 * Add document
	 * @param doc
	 * @return
	 */
	public int add(String doc) {
		bulk.add(doc);
		if (bulk.size() >= bulksize) {
			return indexBulkDoc();
		} else {
			return 0;
		}
	}
	
	public int flush() {
		return indexBulkDoc();
	}
	
	public int indexBulkDoc() {
		BulkRequestBuilder bulkRequest = client.prepareBulk();

		long id = -1;
		Status s = null;
		String json = null;
		String text = null;
		for (int i = 0; i < bulk.size(); i++) {
			json = bulk.get(i);
			try {
				s = TwitterObjectFactory.createStatus(json);
				id = s.getId();
				text = s.getText();
			} catch (TwitterException e) {
				// log error
				continue; // ignore this line
			}
			Map<String, Object> object = new HashMap<String, Object>();
			object.put("text", text);
			object.put("json", json);
			bulkRequest.add(client.prepareIndex(index, type, Long.toString(id)).setSource(object));
		}
		
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		if (bulkResponse.hasFailures()) {
		    // TODO process failures by iterating through each bulk response item
		}
		
		int num = bulkResponse.getItems().length;
		logger.info("Inserted: " + num);
		bulk.clear();
		
		return num;
	}
	
	public GetResponse get(long id) {
		GetResponse response = client.prepareGet(index, type, Long.toString(id))
		        .execute()
		        .actionGet();
		return response;
	}
	
	/**
	 * Given the id, get the referenced tweet.
	 * @param id
	 * @return
	 */
	public SearchResponse get(QueryBuilder qb) {
//		 = QueryBuilders.matchQuery("id", id);
		SearchResponse response  = client.prepareSearch(index).setQuery(qb)
				.execute()
				.actionGet();
		return response;
	}
	
	public DeleteResponse delete() {
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		test();
//		indexTREC2013(new String[]{
//				"/Volumes/ZTZ-99/profession.workspace/trec2013/corpus/trec2013-24hr-test"
//		});
		indexTREC2013(args);
	}
	
	public static void test() {
		// json 1
//		String json = "{\"created_at\": \"Wed Jan 26 10:12:32 +0000 2011\", \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 30206446796808192, \"retweet_count\": 377, \"retweeted\": false, \"retweeted_status\": {\"created_at\": \"Wed Jan 26 09:14:16 +0000 2011\", \"favorite_count\": 23, \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 30191784231444482, \"retweet_count\": 377, \"retweeted\": false, \"source\": \"<a href=\\\"http://www.echofon.com/\\\" rel=\\\"nofollow\\\">Echofon</a>\", \"text\": \"Since 1932 the BBC World Service has done more than any army to promote a stable world. Please hashtag #savews and join us at www.savews.com\", \"truncated\": false, \"user\": {\"created_at\": \"Sat Feb 27 14:01:09 +0000 2010\", \"description\": \"Ranting less frequently these days. Still buying records.\", \"favourites_count\": 2006, \"followers_count\": 3994, \"friends_count\": 769, \"id\": 118067395, \"lang\": \"en\", \"listed_count\": 211, \"name\": \"Paul Lewis\", \"profile_background_color\": \"022330\", \"profile_background_tile\": false, \"profile_image_url\": \"https://si0.twimg.com/profile_images/378800000077775613/063b94c4dadba3abe219569394bb2a2a_normal.jpeg\", \"profile_link_color\": \"0084B4\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/profile_background_images/163070132/ssmsml.jpg\", \"profile_text_color\": \"333333\", \"protected\": false, \"screen_name\": \"RealPaulLewis\", \"statuses_count\": 20068, \"time_zone\": \"London\", \"utc_offset\": 3600}}, \"source\": \"<a href=\\\"http://tweetli.st/\\\" rel=\\\"nofollow\\\">TweetList Pro</a>\", \"text\": \"RT @lewispaul64: Since 1932 the BBC World Service has done more than any army to promote a stable world. Please hashtag #savews and join ...\", \"truncated\": false, \"user\": {\"created_at\": \"Wed Jan 14 11:07:33 +0000 2009\", \"description\": \"Person with ladybits; frustratingly-occasional digital illustrator; ultracrepidarian with own survival plan for zombie apocalypse. Art tweetings @KettlsArt.\", \"favourites_count\": 86, \"followers_count\": 698, \"friends_count\": 777, \"geo_enabled\": true, \"id\": 18973178, \"lang\": \"en\", \"listed_count\": 21, \"location\": \"Loch Lomond, Scotland\", \"name\": \"Jen Kettlewell\", \"profile_background_color\": \"010001\", \"profile_background_tile\": true, \"profile_image_url\": \"https://si0.twimg.com/profile_images/378800000073721211/c102e58c8ea3f2942ab1f0777cb4d1bb_normal.jpeg\", \"profile_link_color\": \"852C4A\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/profile_background_images/718446298/2fd3366c23540ca9f26e7ef9875baaef.jpeg\", \"profile_text_color\": \"C29494\", \"protected\": false, \"screen_name\": \"kettls\", \"statuses_count\": 32752, \"time_zone\": \"Edinburgh\", \"utc_offset\": 3600}}";
		
		// json 2
//		String json = "{\"created_at\": \"Wed Jan 26 09:29:20 +0000 2011\", \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 30195574351335425, \"retweet_count\": 377, \"retweeted\": false, \"retweeted_status\": {\"created_at\": \"Wed Jan 26 09:14:16 +0000 2011\", \"favorite_count\": 23, \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 30191784231444482, \"retweet_count\": 377, \"retweeted\": false, \"source\": \"<a href=\\\"http://www.echofon.com/\\\" rel=\\\"nofollow\\\">Echofon</a>\", \"text\": \"Since 1932 the BBC World Service has done more than any army to promote a stable world. Please hashtag #savews and join us at www.savews.com\", \"truncated\": false, \"user\": {\"created_at\": \"Sat Feb 27 14:01:09 +0000 2010\", \"description\": \"Ranting less frequently these days. Still buying records.\", \"favourites_count\": 2006, \"followers_count\": 3994, \"friends_count\": 769, \"id\": 118067395, \"lang\": \"en\", \"listed_count\": 211, \"name\": \"Paul Lewis\", \"profile_background_color\": \"022330\", \"profile_background_tile\": false, \"profile_image_url\": \"https://si0.twimg.com/profile_images/378800000077775613/063b94c4dadba3abe219569394bb2a2a_normal.jpeg\", \"profile_link_color\": \"0084B4\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/profile_background_images/163070132/ssmsml.jpg\", \"profile_text_color\": \"333333\", \"protected\": false, \"screen_name\": \"RealPaulLewis\", \"statuses_count\": 20068, \"time_zone\": \"London\", \"utc_offset\": 3600}}, \"source\": \"web\", \"text\": \"RT @lewispaul64: Since 1932 the BBC World Service has done more than any army to promote a stable world. Please hashtag #savews and join ...\", \"truncated\": false, \"user\": {\"created_at\": \"Fri Jan 30 18:44:24 +0000 2009\", \"description\": \"Left-whinger. Studied astrophysics many years ago. Like making websites.\", \"favourites_count\": 1093, \"followers_count\": 496, \"friends_count\": 549, \"geo_enabled\": true, \"id\": 19781096, \"lang\": \"en\", \"listed_count\": 15, \"location\": \"London\", \"name\": \"TheoGB\", \"profile_background_color\": \"131516\", \"profile_background_tile\": true, \"profile_image_url\": \"https://si0.twimg.com/profile_images/3672233136/853dd3f8f7a6cbbb6d92fcf3d8d7d489_normal.jpeg\", \"profile_link_color\": \"057D35\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/images/themes/theme14/bg.gif\", \"profile_text_color\": \"E0981D\", \"protected\": false, \"screen_name\": \"TheOGB\", \"statuses_count\": 23250, \"time_zone\": \"London\", \"url\": \"http://t.co/QY67B5U62S\", \"utc_offset\": 3600}}";
		
		// json 30233794111868929
//		String json = "{\"created_at\": \"Wed Jan 26 12:01:12 +0000 2011\", \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 30233794111868929, \"retweet_count\": 377, \"retweeted\": false, \"retweeted_status\": {\"created_at\": \"Wed Jan 26 09:14:16 +0000 2011\", \"favorite_count\": 23, \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 30191784231444482, \"retweet_count\": 377, \"retweeted\": false, \"source\": \"<a href=\\\"http://www.echofon.com/\\\" rel=\\\"nofollow\\\">Echofon</a>\", \"text\": \"Since 1932 the BBC World Service has done more than any army to promote a stable world. Please hashtag #savews and join us at www.savews.com\", \"truncated\": false, \"user\": {\"created_at\": \"Sat Feb 27 14:01:09 +0000 2010\", \"description\": \"Ranting less frequently these days. Still buying records.\", \"favourites_count\": 2006, \"followers_count\": 3994, \"friends_count\": 769, \"id\": 118067395, \"lang\": \"en\", \"listed_count\": 211, \"name\": \"Paul Lewis\", \"profile_background_color\": \"022330\", \"profile_background_tile\": false, \"profile_image_url\": \"https://si0.twimg.com/profile_images/378800000077775613/063b94c4dadba3abe219569394bb2a2a_normal.jpeg\", \"profile_link_color\": \"0084B4\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/profile_background_images/163070132/ssmsml.jpg\", \"profile_text_color\": \"333333\", \"protected\": false, \"screen_name\": \"RealPaulLewis\", \"statuses_count\": 20068, \"time_zone\": \"London\", \"utc_offset\": 3600}}, \"source\": \"web\", \"text\": \"RT @lewispaul64: Since 1932 the BBC World Service has done more than any army to promote a stable world. Please hashtag #savews and join ...\", \"truncated\": false, \"user\": {\"created_at\": \"Fri Jul 25 19:06:51 +0000 2008\", \"description\": \"I do Illustration and Graphic Design. I like people. I'm also quite a fan of eating and then telling you what I've just eaten.\", \"favourites_count\": 1897, \"followers_count\": 604, \"friends_count\": 206, \"geo_enabled\": true, \"id\": 15602181, \"lang\": \"en\", \"listed_count\": 36, \"location\": \"Beautiful Brighton.\", \"name\": \"Emma Charleston\", \"profile_background_color\": \"FFFFFF\", \"profile_background_tile\": true, \"profile_image_url\": \"https://si0.twimg.com/profile_images/1249649338/drawing_normal.jpg\", \"profile_link_color\": \"8C3F00\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/profile_background_images/158753400/newtwitter3.jpg\", \"profile_text_color\": \"000000\", \"protected\": false, \"screen_name\": \"undividual\", \"statuses_count\": 20073, \"time_zone\": \"London\", \"url\": \"http://t.co/pJHRMz2caS\", \"utc_offset\": 3600}}";
		
		// json 31779996569706496
		String json = "{\"created_at\": \"Sun Jan 30 18:25:16 +0000 2011\", \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 31779996569706496, \"retweet_count\": 11, \"retweeted\": false, \"retweeted_status\": {\"created_at\": \"Sun Jan 30 11:43:31 +0000 2011\", \"favorite_count\": 2, \"favorited\": false, \"hashtags\": [\"savews\"], \"id\": 31678894184595457, \"retweet_count\": 11, \"retweeted\": false, \"source\": \"<a href=\\\"http://www.echofon.com/\\\" rel=\\\"nofollow\\\">Echofon</a>\", \"text\": \"Since 1932 the BBC World Service has done more than any army to promote a stable world Please hashtag #savews and join us at www.savews.com\", \"truncated\": false, \"user\": {\"created_at\": \"Sat Feb 27 14:01:09 +0000 2010\", \"description\": \"Ranting less frequently these days. Still buying records.\", \"favourites_count\": 2006, \"followers_count\": 3994, \"friends_count\": 769, \"id\": 118067395, \"lang\": \"en\", \"listed_count\": 211, \"name\": \"Paul Lewis\", \"profile_background_color\": \"022330\", \"profile_background_tile\": false, \"profile_image_url\": \"https://si0.twimg.com/profile_images/378800000077775613/063b94c4dadba3abe219569394bb2a2a_normal.jpeg\", \"profile_link_color\": \"0084B4\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/profile_background_images/163070132/ssmsml.jpg\", \"profile_text_color\": \"333333\", \"protected\": false, \"screen_name\": \"RealPaulLewis\", \"statuses_count\": 20068, \"time_zone\": \"London\", \"utc_offset\": 3600}}, \"source\": \"<a href=\\\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone</a>\", \"text\": \"RT @lewispaul64: Since 1932 the BBC World Service has done more than any army to promote a stable world Please hashtag #savews and join  ...\", \"truncated\": false, \"user\": {\"created_at\": \"Fri Jul 18 21:51:49 +0000 2008\", \"description\": \"Music, radio, gigs, classical, art, London. Semi-regular visits to Newcastle-upon-Tyne & India. Writes about music for @musicOMH\", \"favourites_count\": 869, \"followers_count\": 628, \"friends_count\": 1244, \"geo_enabled\": true, \"id\": 15486896, \"lang\": \"en\", \"listed_count\": 33, \"location\": \"Leyton, London\", \"name\": \"Steven Johnson\", \"profile_background_color\": \"022330\", \"profile_background_tile\": false, \"profile_image_url\": \"https://si0.twimg.com/profile_images/378800000209213421/3a45cfb416addd9af4f18b24a2067bc3_normal.jpeg\", \"profile_link_color\": \"0084B4\", \"profile_sidebar_fill_color\": \"http://a0.twimg.com/images/themes/theme15/bg.png\", \"profile_text_color\": \"333333\", \"protected\": false, \"screen_name\": \"_SPJ_\", \"statuses_count\": 5905, \"url\": \"http://t.co/1vl5OismvG\"}}";
		ElasticSearchUtility esu = new ElasticSearchUtility();
		
		try {
			esu.indexDoc(json, TwitterObjectFactory.createStatus(json).getId());
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		esu.close();
	}
	
	public static void indexTREC2013(String[] args) {
		if (args.length < 3 ) {
			logger.info("Usage: ---");
			return;
		}
		
		ElasticSearchUtility esu = (args.length == 4) ? 
				new ElasticSearchUtility(args[1], args[2], Integer.parseInt(args[3])) : new ElasticSearchUtility(args[1], args[2]);
		
		String filename = args[0]; // directory name
		
		int i = 0;
		File corpusDir = new File(filename);
		for (File file : corpusDir.listFiles()) {
			logger.info("Start processing " + file.getName());
			String line = null;
			try {
				BufferedReader bf = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF8"));
				while ((line = bf.readLine()) != null) {
					int num = esu.add(line.trim());
					if (num != 0) {
						i += num;
						logger.info(i + " tweets indexed.");
					}
				}
				bf.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
