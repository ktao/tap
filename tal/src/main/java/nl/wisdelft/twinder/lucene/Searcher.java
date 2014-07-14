/**
 * 
 */
package nl.wisdelft.twinder.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import nl.wisdelft.twinder.utility.PropertyReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author ktao
 *
 */
public class Searcher {
	
	/** The index from where to search */
	private String indexDir;
	private IndexReader indexReader;
	private QueryParser parser;
	private IndexSearcher searcher;
	
	public Searcher(String indexDir) {
		try {
			indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
			searcher = new IndexSearcher(indexReader);
			searcher.setSimilarity(new LMDirichletSimilarity(2500.0f)); // cf. MSM paper
			parser = new QueryParser(Version.LUCENE_47, "contents", Indexer.ANALYZER);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Searcher() {
		try {
			indexDir = PropertyReader.getString("tal.index.path");
			indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
			searcher = new IndexSearcher(indexReader);
			searcher.setSimilarity(new LMDirichletSimilarity(2500.0f)); // cf. MSM paper
			parser = new QueryParser(Version.LUCENE_47, "contents", Indexer.ANALYZER);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Search with simple keyword query.
	 * @param rawQuery the given query
	 * @param number of results needed
	 * @return Top results
	 */
	public TopDocs search(String rawQuery, int k) {
		try {
	        Query query = parser.parse(rawQuery);
	        return searcher.search(query, null, k);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Search with raw query and expect top 100 results. 
	 * @param rawQuery
	 * @return
	 */
	public TopDocs search(String rawQuery) {
		return search(rawQuery, 100);
	}
	
	/**
	 * @param query
	 * @param k
	 * @return
	 */
	public TopDocs search(Query query, int k) {
		try {
	        return searcher.search(query, null, k);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getDocument(int docid) throws IOException {
		return searcher.doc(docid);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String query = "中国";
//		String query = "\"BBC World Service\"^2 staff cuts";
//		String query = "\"BBC\"^1.25 \"BBC World Service\"^2.0 \"Staff stick\"^0.5 \"Film editing\"^0.5 \"World\"^0.25 \"Service\"^0.25 \"Staff\"^0.25 \"stick\"^0.25 \"Film\"^0.25 \"editing\"^0.25";
		if (args.length > 0) {
			query = args[0];
		}
//		Searcher searcher = new Searcher("/Volumes/ZTZ-99/index/trec2011_lucene_index");
//		Searcher searcher = new Searcher(PropertyReader.getString("tal.index.path"));
		Searcher searcher = new Searcher("/Volumes/ZTZ-99/index/trec2013_lucene_index");
		Date start = new Date();
		TopDocs docs = searcher.search(query);
		Date end = new Date();
		System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
		int rank = 1;
		for (ScoreDoc doc : docs.scoreDocs) {
			int docid = doc.doc;
        	Document tweet;
			try {
				tweet = searcher.getDocument(docid);
				System.out.println(rank++ + " DOCID " + docid + " " + doc.score + " " + tweet.get("id") + " - " + tweet.get("contents"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
//		String index = "/usr/local/twinder/index";
//	    String field = "contents";
//	    String rawQuery = "second";
//	    
//	    IndexReader reader;
//		try {
//			reader = DirectoryReader.open(FSDirectory.open(new File(index)));
//			IndexSearcher searcher = new IndexSearcher(reader);
//			searcher.setSimilarity(new LMDirichletSimilarity(2500.0f));
//		    // :Post-Release-Update-Version.LUCENE_XY:
//		    
//		    QueryParser parser = new QueryParser(Version.LUCENE_47, field, Indexer.ANALYZER);
//		    
//	        Date start = new Date();
//	        Query query = parser.parse(rawQuery);
//	        TopDocs docs = searcher.search(query, null, 100);
//	        
//	        int rank = 1;
//	        for (ScoreDoc doc : docs.scoreDocs) {
//	        	int docid = doc.doc;
//	        	Document tweet = searcher.doc(docid);
//	        	System.out.println(rank++ + " DOCID " + docid + " " + doc.score + " " + tweet.get("id"));
//	        }
//	        
//	        Date end = new Date();
//	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}

}
