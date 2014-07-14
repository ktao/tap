/**
 * 
 */
package nl.wisdelft.twinder.relevance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.wisdelft.twinder.function.SemanticEnrichment;
import nl.wisdelft.twinder.io.JDBCUtility;
import nl.wisdelft.twinder.lucene.Searcher;
import nl.wisdelft.twinder.ml.weka.ClassifierGenerator;
import nl.wisdelft.twinder.tal.model.SemanticEntity;
import nl.wisdelft.twinder.tal.model.SemanticTopic;
import nl.wisdelft.twinder.tal.model.Tweet;
import nl.wisdelft.twinder.tal.model.query.QueryGeneration_DBpedia_MultipleSources;
import nl.wisdelft.twinder.tal.model.query.QueryProfile;
import nl.wisdelft.twinder.tal.model.query.Topic;
import nl.wisdelft.twinder.utility.LanguageClassifier;
import nl.wisdelft.twinder.utility.TweetUtility;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import weka.classifiers.functions.Logistic;
import weka.core.SerializationHelper;

import com.cybozu.labs.langdetect.LangDetectException;

/**
 * This is a 
 * @author ktao
 *
 */
public class ModelGenerator {

	public static Date TWITTER_LAUNCH;
	static {
		try {
			TWITTER_LAUNCH = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2006-07-15");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Only tweets that have been judged are useful for generating the 
	 * classification model.
	 * 
	 *  2 files including the qrels.
	 *  resources/qrels/qrels.microblog2011.txt
	 *  resources/qrels/qrels.microblog2012.txt
	 */
	public static void importTweetsWithQrels(String filename) {
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"INSERT INTO tweets_judged (id, qrel, topicId) VALUES (?, ?, ?)");
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.declareParameter(new SqlParameter("qrel", Types.TINYINT));
		bsu.declareParameter(new SqlParameter("topicId", Types.SMALLINT));
		bsu.compile();
		int count = 0;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = br.readLine()) != null) {
				count ++;
				String[] fields = line.split(" ");
				
				long id = Long.parseLong(fields[2]);
				int qrel = Integer.parseInt(fields[3]);
				int topicId = Integer.parseInt(fields[0].replace("MB", ""));
				
				if (id == 0L) {
					System.out.println(topicId);
				}
				
				bsu.update(new Object[]{
						id,
						qrel,
						topicId
				});
				
				if (count % 100 == 0) {
					bsu.flush();
					System.out.println(count + " lines processed.");
				}
			}
			bsu.flush();
			System.out.println(count + " lines processed.");
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// UPDATE tweets_judged j, tweets t SET j.content = t.content WHERE j.id = t.id; finished
	// May 7 Crawling 1024 missing tweets with python script 
	// May 7 ./fetch-missing-tweets.py tweets2011-missing-id tweets2011-missing-tweets.json
	// May 7 Tweets imported into the table "tweets", updated content field in tweets_judged.
	
	// May 7 Sentiment - bulk results - /Volumes/ZTZ-99/profession.workspace/twinder/sentiment-modelling 
	
	/**
	 * Detect the english tweets and copy them into the new table tweets_en
	 * May 7, the English tweets detected by Python library langid have been
	 * moved to the table of "tweets_en_langid"
	 */
	public static void detectEnglishTweets(long startid) {
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"INSERT IGNORE INTO tweets_en SELECT * FROM tweets WHERE id = ?");
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.compile();
		bsu.setBatchSize(10000);
		
		int count = 0;
		long lastid = startid;
		int i;
		do {
			i = 100000;
			ResultSet rs = JDBCUtility.executeQuerySingleConnection(
					"SELECT id, content "
					+ "FROM tweets WHERE id >= " + lastid + " "
					+ "ORDER BY id ASC "
					+ "LIMIT 100000", JDBCUtility.ds);
			try {
				while (rs.next()) {
					count++;
					i--;
					
					if (LanguageClassifier.getInstance().detect(rs.getString("content")).equals("en")) {
						bsu.update(new Object[]{
							rs.getLong("id")
						});
					}
					
					if(count % 2000 == 0) {
						System.out.println(count + " entries processed.");
						bsu.flush();
					}
					lastid = rs.getLong("id");
				}
				bsu.flush();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
		} while (i == 0);
	}
	
	/**
	 * 
	 */
	public static void prepareSemantics(String[] args) {
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"INSERT INTO semanticsTweetsEntity VALUES(?, ?, ?, ?, ?, ?)");
		bsu.declareParameter(new SqlParameter("tweetId", Types.BIGINT));
		bsu.declareParameter(new SqlParameter("type", Types.VARCHAR));
		bsu.declareParameter(new SqlParameter("typeURI", Types.VARCHAR));
		bsu.declareParameter(new SqlParameter("name", Types.VARCHAR));
		bsu.declareParameter(new SqlParameter("uri", Types.VARCHAR));
		bsu.declareParameter(new SqlParameter("relevance", Types.DOUBLE));
		bsu.compile();
		bsu.setBatchSize(100);
		
		BatchSqlUpdate bsu_topic = new BatchSqlUpdate(
				JDBCUtility.ds,
				"INSERT IGNORE INTO semanticsTweetsTopic (tweetId, topic, uri, relevance) "
						+ " values (?,?,?,?)");
		bsu_topic.declareParameter(new SqlParameter("tweetId", Types.BIGINT)); //$NON-NLS-1$
		bsu_topic.declareParameter(new SqlParameter("topic", Types.VARCHAR)); //$NON-NLS-1$
		bsu_topic.declareParameter(new SqlParameter("uri", Types.VARCHAR)); //$NON-NLS-1$
		bsu_topic.declareParameter(new SqlParameter("relevance", Types.DOUBLE)); //$NON-NLS-1$
		bsu_topic.compile();
		bsu.setBatchSize(100);
		
		SemanticEnrichment se = new SemanticEnrichment(SemanticEnrichment.NER_SERVICE_OPENCALAIS);
		
		int count = 0;
		long lastid = 0L;
		if (args.length == 1) {
			lastid = Long.parseLong(args[0]);
		}
		int i;
		do {
			i = 1000;
			ResultSet rs = JDBCUtility.executeQuerySingleConnection(
					"SELECT DISTINCT id, content "
					+ "FROM tweets_en WHERE id > " + lastid + " "
					+ "ORDER BY id ASC "
					+ "LIMIT 1000", JDBCUtility.ds);
			try {
				while (rs.next()) {
					count++;
					i--;
					
					Tweet t = new Tweet(rs.getLong("id"), rs.getString("content"));
					se.enrichWithOpenCalais(t);
					System.out.println("Processing " + t.getId() + " - " + t.getText());
					if (t.getOcEntities() != null) {
						for (SemanticEntity e : t.getOcEntities()) {
							bsu.update(new Object[]{
								t.getId(),
								e.getType(),
								e.getTypeURI(),
								e.getName(),
								e.getURI(),
								e.getScore()
							});
						}
					}
					
					if (t.getOcTopics() != null) {
						for (SemanticTopic c : t.getOcTopics()) {
							bsu_topic.update(new Object[]{
								t.getId(),
								c.getTopicName(),
								c.getTopicURI(),
								c.getScore()
							});
						}
					}
					
					if(count % 100 == 0) {
						System.out.println(count + " tweets processed.");
						bsu.flush();
						bsu_topic.flush();
					}
					lastid = rs.getLong("id");
				}
				bsu.flush();
				bsu_topic.flush();
				System.out.println(count + " tweets processed.");
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
		} while (i == 0);
	}
	
	/**
	 * For generating the model - update the keywordscore
	 */
	public static void updateKeywordScore() {
		String index = "/Volumes/ZTZ-99/index/trec2011_lucene_index_0507";
		Searcher searcher = new Searcher(index);
		
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"UPDATE tweets_judged "
				+ "SET keywordscore = ? WHERE id = ? AND topicId = ?");
		bsu.declareParameter(new SqlParameter("keywordscore", Types.DOUBLE));
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.declareParameter(new SqlParameter("topicId", Types.INTEGER));
		bsu.compile();
		bsu.setBatchSize(1000);
		
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(
				"SELECT reference, title "
				+ "FROM topic_tweets2011");
		
		try {
			double max = -1.0;
			while (rs.next()) {
				int topicId = Integer.parseInt(rs.getString("reference").replace("MB", ""));
				String title = rs.getString("title");
				Date start = new Date();
				TopDocs docs = searcher.search(title, 10000);
				Date end = new Date();
				System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
				int rank = 1;
				for (ScoreDoc doc : docs.scoreDocs) {
					int docid = doc.doc;
			    	Document tweet;
					try {
						tweet = searcher.getDocument(docid);
						if (rank == 1) {
							max = doc.score;
						}
						System.out.println(rank++ + " DOCID " + docid + " " + doc.score + " " + tweet.get("id"));
						long id = Long.parseLong(tweet.get("id"));
						
						bsu.update(new Object[]{
							doc.score / max,
							id,
							topicId
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				bsu.flush();
				System.out.println("Finished for topic " + topicId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * For generating the model - update the semanticscore
	 */
	public static void updateSemanticScore() {
		String index = "/Volumes/ZTZ-99/index/trec2011_lucene_index_0507";
		Searcher searcher = new Searcher(index);
		
		// expanding the query with NER services - cf. TREC 2011 notebook paper
		QueryGeneration_DBpedia_MultipleSources qdm = new QueryGeneration_DBpedia_MultipleSources();
		
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"UPDATE tweets_judged "
				+ "SET semanticscore = ? WHERE id = ? AND topicId = ?");
		bsu.declareParameter(new SqlParameter("semanticscore", Types.DOUBLE));
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.declareParameter(new SqlParameter("topicId", Types.INTEGER));
		bsu.compile();
		bsu.setBatchSize(1000);
		
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(
				"SELECT reference, title, queryTweetTime "
				+ "FROM topic_tweets2011");
		
		try {
			while (rs.next()) {
				int topicId = Integer.parseInt(rs.getString("reference").replace("MB", ""));
				String title = rs.getString("title");
				long time = rs.getLong("queryTweetTime");
				Date start = new Date();
				
				// ending point ignored.
				Topic t = new Topic(topicId, "Twinder" + topicId, title, time);
				
				QueryProfile q = qdm.generateQueryInRealTime(t);
				System.out.println(q);
				
				TopDocs docs = searcher.search(q.toLuceneWeightQuery(), 10000);
				Date end = new Date();
				System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
				int rank = 1;
				double max = -1.0;
				for (ScoreDoc doc : docs.scoreDocs) {
					int docid = doc.doc;
			    	Document tweet;
					try {
						tweet = searcher.getDocument(docid);
						long id = Long.parseLong(tweet.get("id"));
						if (rank == 1) {
							max = doc.score;
						}
						System.out.println(rank++ + " DOCID " + docid + " " + doc.score + " " + tweet.get("id"));
						
						bsu.update(new Object[]{
							doc.score / max,
							id,
							topicId
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				bsu.flush();
				System.out.println("Finished for topic " + topicId + " - " + t.title);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * May 14th, update syntactical and contextual features.
	 * hasHashtag
	 * isReply
	 * hasURL
	 * nFollowers 52,980,228 Katy Perry, http://fanpagelist.com/category/top_users/view/list/sort/followers/
	 * nLists: 560964 - http://fanpagelist.com/category/top_users/view/list/sort/lists/
	 * tAge
	 * nTweets : log10(nTweets) / 7.0 / http://www.mediabistro.com/alltwitter/most-tweets-per-day_b5394
	 */
	public static void updateSyntacticalFeatures() {
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"UPDATE tweets_judged "
				+ "SET hasHashtag = ?, isReply = ?, hasURL = ?, nFollowers = ?, "
				+ "nLists = ?, tAge =?, nTweets = ? WHERE id = ?");
		bsu.declareParameter(new SqlParameter("hasHashtag", Types.BOOLEAN));
		bsu.declareParameter(new SqlParameter("isReply", Types.BOOLEAN));
		bsu.declareParameter(new SqlParameter("hasURL", Types.BOOLEAN));
		bsu.declareParameter(new SqlParameter("nFollowers", Types.DOUBLE));
		bsu.declareParameter(new SqlParameter("nLists", Types.DOUBLE));
		bsu.declareParameter(new SqlParameter("tAge", Types.DOUBLE));
		bsu.declareParameter(new SqlParameter("nTweets", Types.DOUBLE));
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.compile();
		bsu.setBatchSize(1000);
		
		long lastid = -1;
		int count = 0;
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(
				"SELECT id, content, json "
				+ "FROM tweets_judged WHERE id > " + lastid + " "
				+ "ORDER BY id ASC", JDBCUtility.ds);
		
		Status t = null;
		try {
			while (rs.next()) {
				count++;
				lastid = rs.getLong("id");
				String json = rs.getString("json");
				if (json == null)
					continue;
				t = TwitterObjectFactory.createStatus(json);
				JSONObject tjson = new JSONObject(json);
				JSONObject user = tjson.getJSONObject("user");
				
				bsu.update(new Object[]{
						(t.getHashtagEntities() != null ? (t.getHashtagEntities().length > 0 ? true : false) : false),
						(t.getInReplyToStatusId() != -1 ? true : false),
						(t.getURLEntities() != null ? (t.getURLEntities().length > 0 ? true : false) : false),
						(user.has("followers_count")) ? roundTo1(Math.log10((double)(user.getInt("followers_count"))) / 8.0) : 0.0,
						(user.has("listed_count")) ? roundTo1(Math.log10((double)(user.getInt("listed_count"))) / 5.0) : 0.0,
						(float)(t.getCreatedAt().getTime() - t.getUser().getCreatedAt().getTime()) 
							/ (float)(t.getCreatedAt().getTime() - TWITTER_LAUNCH.getTime()),
						(user.has("statuses_count")) ? roundTo1(Math.log10((double)(user.getInt("statuses_count"))) / 7.0) : 0.0,
						t.getId()
				});
				
				if (count % 1000 == 0)
					System.out.println(count + " finished.");
//				System.out.println("hasHashtag - " + (t.getHashtagEntities() != null ? (t.getHashtagEntities().length > 0 ? true : false) : false));
//				System.out.println("isReply - " + (t.getInReplyToStatusId() != -1 ? true : false));
//				System.out.println("hasURL - " + (t.getURLEntities() != null ? (t.getURLEntities().length > 0 ? true : false) : false));
//				System.out.println("nFollowers - " + roundTo1(Math.log10(t.getUser().getFollowersCount()) / 8.0));
//				System.out.println("nLists - " + Math.log10(t.getUser().getListedCount()) / 5.0);
//				System.out.println("tAge - " + t.getUser().getCreatedAt());
//				System.out.println("nTweets - " + Math.log10(t.getUser().getStatusesCount()) / 7.0);
//				System.out.println("id - " + t.getId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (java.lang.NullPointerException e) {
//			System.out.println(t.getHashtagEntities());
		} catch (JSONException e) {
			System.err.println("Error at " + lastid);
			e.printStackTrace();
		}
		bsu.flush();
	}
	
	/**
	 * 
	 */
	public static void importSentimentResults() {
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"UPDATE tweets_judged "
				+ "SET sentiment140 = ?, sentiment140_lang = ? WHERE id = ?");
		bsu.declareParameter(new SqlParameter("sentiment140", Types.INTEGER));
		bsu.declareParameter(new SqlParameter("sentiment140_lang", Types.VARCHAR));
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.compile();
		bsu.setBatchSize(1000);
		
		String resultFolder = "/Volumes/ZTZ-99/profession.workspace/twinder/sentiment-modelling/results";
		
		File resultDir = new File(resultFolder);
		BufferedReader br;
		for (File file : resultDir.listFiles()) {
			try {
				br = new BufferedReader(new FileReader(file));
				
				String jsonString = br.readLine();
				JSONObject json = new JSONObject(jsonString);
				JSONArray tweets = json.getJSONArray("data");
				for (int i = 0; i < tweets.length(); i++) {
					JSONObject r = tweets.getJSONObject(i);
					long id = r.getLong("id");
					int polarity = r.getInt("polarity");
					String lang = r.getJSONObject("meta").getString("language");
					
					bsu.update(new Object[]{
						polarity,
						lang,
						id
					});
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println(file + " finished.");
		}
		bsu.flush();
	}
	
	/**
	 * semantics --> number of entities, diversity, sentiment
	 * nEntities
	 * diversity
	 * negaSent
	 * posiSent
	 * neutralSent
	 * TODO implement
	 */
	public static void updateSemanticFeatures() {
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"UPDATE tweets_judged "
				+ "SET negaSent = ?, posiSent = ?, "
				+ "neutralSent = ? WHERE id = ?");
		bsu.declareParameter(new SqlParameter("negaSent", Types.BOOLEAN));
		bsu.declareParameter(new SqlParameter("posiSent", Types.BOOLEAN));
		bsu.declareParameter(new SqlParameter("neutralSent", Types.BOOLEAN));
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.compile();
		bsu.setBatchSize(1000);
		
		long lastid = -1;
		int count = 0;
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(
				"SELECT id, sentiment140 "
				+ "FROM tweets_judged "
				+ "WHERE id > " + lastid + " ", JDBCUtility.ds);
		System.out.println("Begin updating sentiments...");
		try {
			while (rs.next()) {
				count++;
				
				boolean pos = (rs.getInt("sentiment140") == 4) ? true : false;
				boolean neg = (rs.getInt("sentiment140") == 0) ? true : false;
				boolean neu = (rs.getInt("sentiment140") == 2) ? true : false;
				
				bsu.update(new Object[]{
						neg,
						pos,
						neu,
						rs.getLong("id")
				});
				
				if (count % 1000 == 0)
					System.out.println(count + " finished.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.lang.NullPointerException e) {
//			System.out.println(t.getHashtagEntities());
		}
		bsu.flush();
		
		bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"UPDATE tweets_judged "
				+ "SET nEntities = ?, diversity = ? WHERE id = ?");
		bsu.declareParameter(new SqlParameter("nEntities", Types.INTEGER));
		bsu.declareParameter(new SqlParameter("diversity", Types.INTEGER));
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.compile();
		bsu.setBatchSize(1000);
		System.out.println("Begin updating semantics...");
		lastid = -1;
		count = 0;
		rs = JDBCUtility.executeQuerySingleConnection(
				"SELECT tweetId, COUNT(DISTINCT uri), COUNT(DISTINCT type) "
				+ "FROM semanticsTweetsEntity "
				+ "WHERE tweetId > " + lastid + " "
				+ "GROUP BY tweetId", JDBCUtility.ds);
		
		try {
			while (rs.next()) {
				count++;
				
				bsu.update(new Object[]{
						rs.getInt(2),
						rs.getInt(3),
						rs.getLong("tweetId")
				});
				
				if (count % 1000 == 0)
					System.out.println(count + " finished.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.lang.NullPointerException e) {
//			System.out.println(t.getHashtagEntities());
		}
		bsu.flush();
	}
	
	public static double roundTo1(double value) {
		if (value > 1.0)
			return 1.0;
		else if (value < 0.0)
			return 0.0;
		else
			return value;
	}
	
	/**
	 * keyword-based score --> look up in Lucene documentation
	 * semantic-based score --> look up in Lucene documentation
	 * 	- semantic overlap - Add topic to the entries
	 * 
	 * Syntactical:
	 * 	- hashtag, url, isreply, length --> will be ready in minutes
	 *  - semantics --> number of entities, diversity, sentiment
	 * 
	 * Contextual:
	 * 	- # followers, # lists, Twitter age, # tweets
	 */
	public static void exportARFFFile() {
		String arffFile = "/Users/ktao/Dropbox/phd.thesis/twinder/relevance/trec2011-ground-truth.arff";
		ResultSet rs = JDBCUtility.executeQuerySingleConnection("SELECT * FROM tweets_judged "
				+ "WHERE english IS NOT NULL AND json IS NOT NULL");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(arffFile));
			
			// The data file definition
			bw.write("@RELATION TWINDER_TREC2011_GROUND_TRUTH\n");
			bw.write("\n\n");
			bw.write("@ATTRIBUTE keyword_score	NUMERIC\n");
			bw.write("@ATTRIBUTE semantic_score	NUMERIC\n");
			bw.write("@ATTRIBUTE hasHashtag		{0,1}\n");
			bw.write("@ATTRIBUTE hasURL			{0,1}\n");
			bw.write("@ATTRIBUTE isReply		{0,1}\n");
			bw.write("@ATTRIBUTE length			NUMERIC\n");
			bw.write("@ATTRIBUTE nEntities		NUMERIC\n"); // max 12
			bw.write("@ATTRIBUTE diversity		NUMERIC\n"); // max 7
			bw.write("@ATTRIBUTE positiveSent	{0,1}\n");
			bw.write("@ATTRIBUTE negativeSent	{0,1}\n");
			bw.write("@ATTRIBUTE neutralSent	{0,1}\n");
			bw.write("@ATTRIBUTE nFollowers		NUMERIC\n");
			bw.write("@ATTRIBUTE nLists			NUMERIC\n");
			bw.write("@ATTRIBUTE twitterAge		NUMERIC\n");
			bw.write("@ATTRIBUTE nTweets		NUMERIC\n");
			bw.write("@ATTRIBUTE isRelevant		{1,0}\n");
			bw.write("\n\n");
			
			bw.write("@DATA\n");
			while (rs.next()) {
				bw.write(rs.getDouble("keywordscore") + ",");
				bw.write(rs.getDouble("semanticscore") + ",");
				bw.write((rs.getBoolean("hasHashtag") ? 1 : 0) + ",");
				bw.write((rs.getBoolean("hasURL") ? 1 : 0) + ",");
				bw.write((rs.getBoolean("isReply") ? 1 : 0) + ",");
				bw.write(((double)rs.getInt("length")) / 140.0 + ",");
				bw.write(((double)rs.getInt("nEntities")) / 12.0 + ","); // max 12
				bw.write(((double)rs.getInt("diversity")) / 7.0	+ ","); // max 7
				bw.write((rs.getBoolean("posiSent") ? 1 : 0) + ",");
				bw.write((rs.getBoolean("negaSent") ? 1 : 0) + ",");
				bw.write((rs.getBoolean("neutralSent") ? 1 : 0) + ",");
				bw.write(rs.getDouble("nFollowers") + ",");
				bw.write(rs.getDouble("nLists") + ",");
				bw.write(rs.getDouble("tAge") + ",");
				bw.write(rs.getDouble("nTweets") + ",");
				bw.write((rs.getInt("qrel") > 0 ? 1 : 0) + ",");
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateLength() {
		BatchSqlUpdate bsu = new BatchSqlUpdate(JDBCUtility.ds, 
				"UPDATE tweets_judged "
				+ "SET length = ? WHERE id = ?");
		bsu.declareParameter(new SqlParameter("length", Types.INTEGER));
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.compile();
		bsu.setBatchSize(1000);
		
		long lastid = -1;
		int count = 0;
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(
				"SELECT id, content "
				+ "FROM tweets_judged "
				+ "WHERE id > " + lastid + " ", JDBCUtility.ds);
		System.out.println("Begin updating length...");
		try {
			while (rs.next()) {
				count++;
				
				int length = TweetUtility.preProcessTweet(rs.getString("content")).length();
				
				bsu.update(new Object[]{
						length,
						rs.getLong("id")
				});
				
				if (count % 1000 == 0)
					System.out.println(count + " finished.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.lang.NullPointerException e) {
//			System.out.println(t.getHashtagEntities());
		}
		bsu.flush();
	}
	
	public static void exportModelFile(String datasetFile, String modelFile) {
		String cost_matrix = "\"[0.0 3.5; "
				  + "1.0 0.0]\"";
		String configuration = "-cost-matrix " + cost_matrix + " -S 1 -W "
						+ "weka.classifiers.functions.Logistic -- -R 1.0E-8 -M -1";
		
		Logistic model =  ClassifierGenerator.getClassifier(
				datasetFile, 
				configuration);
		
		try {
			SerializationHelper.write(modelFile, model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * List of features:
	 * 
	 * keyword-based score --> look up in Lucene documentation
	 * semantic-based score --> look up in Lucene documentation
	 * 	- semantic overlap - Add topic to the entries
	 * 
	 * Syntactical:
	 * 	- hashtag, url, isreply, length --> will be ready in minutes
	 *  - semantics --> number of entities, diversity, sentiment
	 * 
	 * Contextual:
	 * 	- # followers, # lists, Twitter age, # tweets
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		importTweetsWithQrels("./resources/qrels/qrels.microblog2011.txt");
//		importTweetsWithQrels("./resources/qrels/qrels.microblog2012.txt");
		
//		detectEnglishTweets(33038400671973376L);
//		prepareSemantics(args);
//		updateKeywordScore();
//		updateSemanticScore();
		
//		updateSyntacticalFeatures();
//		importSentimentResults();
//		updateLength();
//		updateSemanticFeatures();
		
//		exportARFFFile();
		
		// May 15th
//		exportModelFile("/Users/ktao/Dropbox/phd.thesis/twinder/relevance/trec2011-ground-truth.arff", 
//				"/Users/ktao/Dropbox/phd.thesis/twinder/relevance/trec2011-ground-truth.model");
	}

}
