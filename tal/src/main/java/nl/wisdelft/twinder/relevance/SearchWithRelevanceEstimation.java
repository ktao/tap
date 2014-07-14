/**
 * 
 */
package nl.wisdelft.twinder.relevance;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import nl.wisdelft.twinder.TwinderPreprocess;
import nl.wisdelft.twinder.io.MongoDBUtility;
import nl.wisdelft.twinder.lucene.Searcher;
import nl.wisdelft.twinder.tal.model.query.QueryGeneration_DBpedia_MultipleSources;
import nl.wisdelft.twinder.utility.NumberFormatUtility;
import nl.wisdelft.twinder.utility.PropertyReader;
import nl.wisdelft.twinder.utility.TweetUtility;

import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This class will provide with the search function with Relevance Estimation techniques introduced
 * in our TREC 2011 notebook paper, MSM 2012 workshop paper, as well as Twinder ICWE 2012 paper.
 * 
 * @author ktao
 *
 */
public class SearchWithRelevanceEstimation {
	
	private final static Logger logger = LoggerFactory.getLogger(TwinderPreprocess.class);
	
	public static Date TWITTER_LAUNCH;
	static {
		try {
			TWITTER_LAUNCH = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2006-07-15");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static FastVector attributes;
	static {
		 // Declare two numeric attributes

		FastVector fvBoolVal = new FastVector(2);
		fvBoolVal.addElement("0");
		fvBoolVal.addElement("1");

		Attribute attr_keyword_score = new Attribute("keyword_score");
		Attribute attr_semantic_score = new Attribute("semantic_score");
		Attribute attr_hasHashtag = new Attribute("hasHashtag", fvBoolVal);
		Attribute attr_hasURL = new Attribute("hasURL", fvBoolVal);
		Attribute attr_isReply = new Attribute("isReply", fvBoolVal);
		Attribute attr_length = new Attribute("length");
		Attribute attr_nEntities = new Attribute("nEntities");
		Attribute attr_diversity = new Attribute("diversity");
		Attribute attr_positiveSent = new Attribute("positiveSent", fvBoolVal);
		Attribute attr_negativeSent = new Attribute("negativeSent", fvBoolVal);
		Attribute attr_neutralSent = new Attribute("neutralSent", fvBoolVal);
		Attribute attr_nFollowers = new Attribute("nFollowers");
		Attribute attr_nLists = new Attribute("nLists");
		Attribute attr_twitterAge = new Attribute("twitterAge");
		Attribute attr_nTweets = new Attribute("nTweets");
		
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("1");
		fvClassVal.addElement("0");
		Attribute classAttribute = new Attribute("isRelevant", fvClassVal);
		
		// Declare the feature vector
		attributes = new FastVector(16);
		attributes.addElement(attr_keyword_score);    
		attributes.addElement(attr_semantic_score);    
		attributes.addElement(attr_hasHashtag);
		attributes.addElement(attr_hasURL);    
		attributes.addElement(attr_isReply);    
		attributes.addElement(attr_length);
		attributes.addElement(attr_nEntities);    
		attributes.addElement(attr_diversity);    
		attributes.addElement(attr_positiveSent);
		attributes.addElement(attr_negativeSent);    
		attributes.addElement(attr_neutralSent);    
		attributes.addElement(attr_nFollowers);
		attributes.addElement(attr_nLists);    
		attributes.addElement(attr_twitterAge);    
		attributes.addElement(attr_nTweets);
		attributes.addElement(classAttribute);
	}
	
	private Classifier classifier = null;
	
	private Searcher searcher = null;
	
	public SearchWithRelevanceEstimation(String file) {
		try {
			classifier = (Classifier) weka.core.SerializationHelper.read(file);
		} catch (Exception e) {
			logger.error("Loading classifier file failed from " + file);
		}
		
		searcher = new Searcher();
	}
	
	public SearchWithRelevanceEstimation() {
		String file = PropertyReader.getString("tal.model.relevance.file");
		try {
			classifier = (Classifier) weka.core.SerializationHelper.read(file);
		} catch (Exception e) {
			logger.error("Loading classifier file failed from " + file);
		}
		
		searcher = new Searcher();
	}
	
	/**
	 * Given the raw string query, it will expand the query with multiple NER services and make an 
	 * expanded one. Then it will search with both original and expanded queries and output the
	 * final one.
	 * @param query
	 */
	public BasicDBObject[] search(String query) {
		System.out.println("Search started for - " + query);
		
		// keyword-based search
		TopDocs kb_results = searcher.search(query);
		BasicDBObject[] results = new BasicDBObject[kb_results.scoreDocs.length];
		// semantic-based search
		QueryGeneration_DBpedia_MultipleSources qdm = new QueryGeneration_DBpedia_MultipleSources();
		TopDocs sb_results = searcher.search(qdm.generateQueryInRealTime(query).toLuceneWeightQuery());
		
		// prepare keyword and semantic -based scores HashTable
		HashMap<Long, Double> sb_scores = new HashMap<Long, Double>();
		double max_kb_score = kb_results.scoreDocs[0].score;
		double max_sb_score = sb_results.scoreDocs[0].score;
		for (int i = 0; i < sb_results.scoreDocs.length; i++) {
			try {
				sb_scores.put(Long.parseLong((String)searcher.getDocument(sb_results.scoreDocs[i].doc).get("id")), 
						sb_results.scoreDocs[i].score / max_sb_score);
			} catch (NumberFormatException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// load unlabeled data
		Instances unlabeled = new Instances("Twinder-Search" + query, attributes, 0); // new BufferedReader(new FileReader("/some/where/unlabeled.arff"))
		
		for (int i = 0; i < kb_results.scoreDocs.length; i++) {
			long id = -1;
			try {
				id = Long.parseLong((String)searcher.getDocument(kb_results.scoreDocs[i].doc).get("id"));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			double sb_score = sb_scores.containsKey(id) ? sb_scores.get(id) : 0.0;
			BasicDBObject t = (BasicDBObject) MongoDBUtility.getTweet(id);
			unlabeled.add(getTweetInstance(t, kb_results.scoreDocs[i].score / max_kb_score, sb_score));
			unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
			
			try {
				double clsLabel = classifier.classifyInstance(unlabeled.instance(0));
				if (clsLabel == 0.0) {
					// relevant
//					System.out.println("===relevant===");
					t.append("relevance", true);
				} else if (clsLabel == 1.0) {
					// non-relevant
//					System.out.println("==irrelevant==");
					t.append("relevance", false);
				}
//				System.out.println(t.getLong("id") + " - " + t.getString("content"));
				results[i] = t;
			} catch (Exception e) {
				e.printStackTrace();
			}
			unlabeled.delete();
		}
		return results;
	}
	
	/**
	 * Prepare an Instance that can be classified with pre-trained model.
	 * @return
	 */
	public Instance getTweetInstance(BasicDBObject t, double kscore, double sscore) {
		BasicDBObject u = (BasicDBObject) t.get("user");
		Instance inst = new Instance(16);
		
		FastVector attributes = SearchWithRelevanceEstimation.attributes;
		inst.setValue((Attribute)attributes.elementAt(0), kscore);
		inst.setValue((Attribute)attributes.elementAt(1), sscore);
		inst.setValue((Attribute)attributes.elementAt(2), 
				(!t.containsField("urls") && ((BasicDBList)t.get("urls")).size() == 0) ? "0" : "1");
		inst.setValue((Attribute)attributes.elementAt(3), 
				(!t.containsField("hashtags") && ((BasicDBList)t.get("hashtags")).size() == 0) ? "0" : "1");
		inst.setValue((Attribute)attributes.elementAt(4), 
				t.containsField("in_reply_to_status_id") ? "1" : "0");
		inst.setValue((Attribute)attributes.elementAt(5), 
				(double)TweetUtility.preProcessTweet(t.getString("content")).length() / 140.0);
		if (t.containsField("semanticEntities")) {
			HashSet<String> types = new HashSet<String>();
			BasicDBList entities = (BasicDBList)t.get("semanticEntities");
			for (int count = 0; count < entities.size(); count++) {
				types.add(((BasicDBObject)entities.get(count)).getString("type"));
			}
			inst.setValue((Attribute)attributes.elementAt(6), 
					(double)entities.size() / 12.0);
			inst.setValue((Attribute)attributes.elementAt(7), 
					(double)types.size() / 7.0);
		} else {
			inst.setValue((Attribute)attributes.elementAt(6), 0.0);
			inst.setValue((Attribute)attributes.elementAt(7), 0.0);
		}
		inst.setValue((Attribute)attributes.elementAt(8), 
				t.getInt("sentiment") == 4 ? "1" : "0");
		inst.setValue((Attribute)attributes.elementAt(9), 
				t.getInt("sentiment") == 0 ? "1" : "0");
		inst.setValue((Attribute)attributes.elementAt(10), 
				t.getInt("sentiment") == 2 ? "1" : "0");
		inst.setValue((Attribute)attributes.elementAt(11), 
				NumberFormatUtility.roundTo1(u.getInt("followers_count") == 0 ? 0.0 : Math.log10((double)(u.getInt("followers_count"))) / 8.0));
		inst.setValue((Attribute)attributes.elementAt(12), 
				NumberFormatUtility.roundTo1(u.getInt("listed_count") == 0 ? 0.0 : Math.log10((double)(u.getInt("listed_count"))) / 5.0));
		inst.setValue((Attribute)attributes.elementAt(13), 
				(float)(t.getDate("created_at").getTime() - u.getDate("created_at").getTime()) 
				/ (float)(t.getDate("created_at").getTime() - TWITTER_LAUNCH.getTime()));
		inst.setValue((Attribute)attributes.elementAt(14), 
				NumberFormatUtility.roundTo1(u.getInt("statuses_count") == 0 ? 0.0 : Math.log10((double)(u.getInt("statuses_count"))) / 7.0));
//		inst.setValue((Attribute)attributes.elementAt(0), 1.0); // is relevant
		return inst;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		System.out.println(MongoDBUtility.getTweet(467339499929092096L).toString());
//		SearchWithRelevanceEstimation sre = new SearchWithRelevanceEstimation("/Users/ktao/Dropbox/phd.thesis/twinder/relevance/trec2011-ground-truth.model");
		SearchWithRelevanceEstimation sre = new SearchWithRelevanceEstimation();
		BasicDBObject[] results = sre.search("BBC World Service staff cut");
		for (BasicDBObject object : results) {
			System.out.println("id - " + object.getLong("id"));
			System.out.println("content - " + object.getLong("content"));
			System.out.println("screenName - " + object.getLong("screenName"));
			System.out.println("profile_image_url - " + object.getLong("profile_image_url"));
			System.out.println("relevance - " + object.getLong("relevance"));
		}
	}

}
