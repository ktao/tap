/**
 * 
 */
package nl.wisdelft.twinder.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.Version;

import twitter4j.json.DataObjectFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * @author ktao
 *
 */
public class Indexer {

	public static final Analyzer ANALYZER = new TweetAnalyzer(Version.LUCENE_47);
	
	private String indexLocation;
	private Directory indexDir;
	private IndexWriterConfig iwc;
	private IndexWriter writer;
	
	/**
	 * Initialize the indexer, so that the new tweets can be appended incrementally.
	 * @param indexPath the path to the index
	 */
	public Indexer(String indexPath) {
		try {
			boolean create = false;
			indexDir = FSDirectory.open(new File(indexPath));
			if (!DirectoryReader.indexExists(indexDir))
				create = true;
			iwc = new IndexWriterConfig(Version.LUCENE_47, Indexer.ANALYZER);
			
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
//		indexTREC2011(new String[]{
//				"/Users/ktao/Desktop/trec2011-judged-id-content",
//				"/Volumes/ZTZ-99/index/trec2011_lucene_index_0507"
//				"/Volumes/ZTZ-99/index/trec2011_lucene_index"
//		});
		try {
			DirectoryReader dirReader = DirectoryReader.open(FSDirectory.open(new File("/Volumes/ZTZ-99/index/trec2013_lucene_index")));
			System.out.println(dirReader.numDocs());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void indexTREC2011(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: ---");
			return;
		}
		String filename = args[0]; // file name
		String indexPath = args[1]; // path to the index
		
		Indexer indexer = new Indexer(indexPath);
		HashMap<Long, String> tweets = new HashMap<Long, String>();
		
		int i = 0;
		try {
			BufferedReader bf = new BufferedReader(new FileReader(new File(filename)));
			String line;
			while ((line = bf.readLine()) != null) {
				Long id = Long.parseLong(line.substring(0, line.indexOf(' ')));
				String content = line.substring(line.indexOf(' ')).trim();
				tweets.put(id, content);
				i++;
				//indexer.indexTweet(id, content);
				if (i % 1000 == 0) {
					indexer.indexTweets(tweets);
					System.out.println(tweets.size() + " tweets added; " + i + " tweets added.");
					tweets.clear();
				}
			}
			
			indexer.close();
			bf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
