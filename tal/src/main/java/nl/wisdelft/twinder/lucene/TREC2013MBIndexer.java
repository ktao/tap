/**
 * 
 */
package nl.wisdelft.twinder.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;

import nl.wisdelft.twinder.tal.model.Tweet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter.IndexReaderWarmer;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.json.DataObjectFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.jcraft.jzlib.GZIPInputStream;

/**
 * This program index the Tweets in JSON bzipped format.
 * @author ktao
 *
 */
public class TREC2013MBIndexer {

	public static final Analyzer ANALYZER = new TweetAnalyzer(Version.LUCENE_47);
	
	private String indexLocation;
	private Directory indexDir;
	private IndexWriterConfig iwc;
	private IndexWriter writer;
	
	/**
	 * Initialize the indexer, so that the new tweets can be appended incrementally.
	 * @param indexPath the path to the index
	 */
	public TREC2013MBIndexer(String indexPath) {
		try {
			boolean create = false;
			indexDir = FSDirectory.open(new File(indexPath));
			if (!DirectoryReader.indexExists(indexDir))
				create = true;
			iwc = new IndexWriterConfig(Version.LUCENE_47, TREC2013MBIndexer.ANALYZER);
			
			if (create) { // Create a new index in the directory, removing any
		        // previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else { // Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);	
			}
			
			
			// Optional: Increase the memory size to 256MB
			iwc.setRAMBufferSizeMB(256.0);
			
			writer = new IndexWriter(indexDir, iwc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		indexTREC2013(new String[]{
				"/Volumes/ZTZ-99/profession.workspace/trec2013/corpus/trec2013", // bunch of gz file
				"/Volumes/ZTZ-99/index/trec2013_lucene_index"
		});
	}
	
	/**
	 * The actual working code for the specified purpose.
	 * 
	 * @param args The first string should be the directory in which all the gzipped files are
	 * stored. The second string should be the location at where the index should be created.
	 */
	public static void indexTREC2013(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: ---");
			return;
		}
		String filename = args[0]; // directory name
		String indexPath = args[1]; // path to the index
		
		TREC2013MBIndexer indexer = new TREC2013MBIndexer(indexPath);
		HashMap<Long, String> tweets = new HashMap<Long, String>();
		
		int i = 0;
		File corpusDir = new File(filename);
		for (File file : corpusDir.listFiles()) {
			System.out.println("Start processing " + file.getName());
			String line = null;
			try {
				BufferedReader bf = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF8"));
				while ((line = bf.readLine()) != null) {
					Status s = null;
					try {
						s = TwitterObjectFactory.createStatus(line);
					} catch (TwitterException e) {
						continue;
					}
					Long id = -1L;
					String content = null;
					id = s.getId();
					content = s.getText();
					if (s == null || id == -1 || content == null) {
						continue;
					}
					tweets.put(id, content);
					i++;
					//indexer.indexTweet(id, content);
					if (i % 1000 == 0) {
						indexer.indexTweets(tweets);
						System.out.println(tweets.size() + " tweets added; " + i + " tweets added.");
						tweets.clear();
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
		indexer.indexTweets(tweets);
		indexer.close();
	}
	
//	static void indexTweets(IndexWriter writer, Long id, String contents) {
//	}
	
	/**
	 * index a group of tweets in batch
	 * @param tweets
	 */
	public void indexTweets(HashMap<Long, String> tweets) {
//		IndexWriter writer;
		try {
//			writer = new IndexWriter(indexDir, iwc);
			
			final FieldType textOptions = new FieldType();
		    textOptions.setIndexed(true);
		    textOptions.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		    textOptions.setStored(true);
		    textOptions.setTokenized(true);  
		    
		    for (Long id : tweets.keySet()) {
		    	Document tweet = new Document();
		    	
		    	// the id of the tweet, should be searchable
				tweet.add(new LongField("id", id, Field.Store.YES));
				// the content of the tweet, will be tokenized
				tweet.add(new Field("contents", tweets.get(id), textOptions));
					
				writer.addDocument(tweet);
			}
		    
		    writer.commit();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * 
	 * @param tweets
	 */
	public int indexTweetObjects(HashMap<Long, Tweet> tweets) {
		int i = 0;
		try {
//			writer = new IndexWriter(indexDir, iwc);
			
			final FieldType textOptions = new FieldType();
		    textOptions.setIndexed(true);
		    textOptions.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		    textOptions.setStored(true);
		    textOptions.setTokenized(true);  
		    
		    for (Long id : tweets.keySet()) {
		    	Document tweet = new Document();
		    	
		    	// the id of the tweet, should be searchable
				tweet.add(new LongField("id", id, Field.Store.YES));
				// the content of the tweet, will be tokenized
				tweet.add(new Field("contents", tweets.get(id).getText(), textOptions));
					
				writer.addDocument(tweet);
				i++;
			}
		    
		    writer.commit();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
		return i;
	}
	

	/**
	 * index a single tweet
	 * @param id
	 * @param contents
	 */
	public void indexTweet(Long id, String contents) {
		try {
//			writer = new IndexWriter(indexDir, iwc);
			Document tweet = new Document();
			final FieldType textOptions = new FieldType();
		    textOptions.setIndexed(true);
		    textOptions.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		    textOptions.setStored(true);
		    textOptions.setTokenized(true);  
		    
			// the id of the tweet, should be searchable
			tweet.add(new LongField("id", id, Field.Store.YES));
			// the content of the tweet, will be tokenized
			tweet.add(new Field("contents", contents, textOptions));
				
			writer.addDocument(tweet);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
	}
	
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
