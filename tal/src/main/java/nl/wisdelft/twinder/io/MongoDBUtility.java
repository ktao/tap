/**
 * 
 */
package nl.wisdelft.twinder.io;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wisdelft.twinder.tal.model.Tweet;
import nl.wisdelft.twinder.utility.PropertyReader;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * The basic interface for accessing the data in MongoDB 
 * @author ktao
 *
 */
public class MongoDBUtility {
	
	private final static Logger logger = LoggerFactory.getLogger(MongoDBUtility.class);

	// To directly connect to a single MongoDB server (note that this will not auto-discover the primary even
	// if it's a member of a replica set:
//	MongoClient mongoClient = new MongoClient();
	// or
	
//	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
//	// or, to connect to a replica set, with auto-discovery of the primary, supply a seed list of members
//	MongoClient mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost", 27017),
//	                                      new ServerAddress("localhost", 27018),
//	                                      new ServerAddress("localhost", 27019)));
	private static MongoClient mongoClient;
	private static DB db;
	private static String tcName;
	static {
		try {
			mongoClient = new MongoClient(PropertyReader.getString("tal.db.mongodb.host")); //"localhost"
			db = mongoClient.getDB(PropertyReader.getString("tal.db.mongodb.database")); // trec2011
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		tcName = PropertyReader.getString("tal.db.mongodb.collection.tweet");
	}
	
	/**
	 * Get a single tweet from MongoDB
	 * @param id
	 * @return
	 */
	public static DBObject getTweet(long id) {
		BasicDBObject query = new BasicDBObject("id", id);
		DBCollection coll = db.getCollection(tcName);
		DBCursor cursor = coll.find(query);
		try {
			while(cursor.hasNext()) {
				return cursor.next();
			}
		} finally {
			cursor.close();
		}
		return null;
	}
	
	public static void main(String[] args) {
//		DBObject doc = getTweet(28965131362770944L);
//		System.out.println(doc.get("contents"));
//		insertIDContent(args);
		
//		BasicDBObject doc = new BasicDBObject("id", 28965131362770944L).
//				append("contents", "Chef salad is calling my name, I'm so hungry!");
//		System.out.println(doc.get("id"));
//		System.out.println(doc.get("contents"));
	}
	
	public static void test(String[] args) {
		// Output the list of the collections
		Set<String> colls = db.getCollectionNames();
		for (String s : colls) {
			System.out.println(s);
		}
				
		if (!db.collectionExists("test")) {
			db.createCollection("test", null);
		}
				
		DBCollection coll = db.getCollection("test");
			
		BasicDBObject doc = new BasicDBObject("id", 28965131362770944L).
				append("contents", "Chef salad is calling my name, I'm so hungry!");
				
		coll.insert(doc);
	}
	
	public static void initializeCollection() {
		if (!db.collectionExists(tcName)) {
			db.createCollection(tcName, null);
			db.getCollection(tcName).createIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));;
			logger.info("Collection " + tcName + " created, index created on id.");
		} else {
			logger.info("Collection " + tcName + " loaded.");
		}
	}
	
	/**
	 * The document to be inserted
	 * @param doc
	 */
	public static int insertTweets(Collection<Tweet> tweets) {
		initializeCollection();
		
		DBCollection coll = db.getCollection(tcName);
		
		int i = 0;
		for (Tweet t : tweets) {
			coll.insert(t.toBasicDBObject(Tweet.MONGODB_STORE_LEVEL_2));
			i += 1;
		}
		return i;
	}
	
	/**
	 * reserved for first prototype
	 * @param args
	 */
	public static void insertIDContent(String[] args) {
		if (!db.collectionExists("tweets_001")) {
			db.createCollection("tweets_001", null);
		}
		
		DBCollection coll = db.getCollection("tweets_001"); // 001 = id + content
		
		ResultSet rs = JDBCUtility.executeQuerySingleConnection(
				"SELECT id, content FROM tweets_en", 
				JDBCUtility.ds);
		
		int i = 0;
		try {
			while (rs.next()) {
				BasicDBObject doc = new BasicDBObject("id", rs.getLong("id"));
				doc.append("content", rs.getString("content"));
				
				coll.insert(doc);
				i += 1;
				if (i % 10000 == 0)
					System.out.println(i + " lines inserted.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Date and Time
		// Date now = new Date();
		// BasicDBObject time = new BasicDBObject("ts", now);
		// collection.save(time);
	}

	public static BasicDBList convertArray(DBObjectCovertable[] objects) {
		BasicDBList list = new BasicDBList();
		for (DBObjectCovertable object : objects) {
			list.add(object.toBasicDBObject());
		}
		return list;
	}
}
