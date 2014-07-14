/*********************************************************
*  Copyright (c) 2011 by Web Information Systems (WIS) Group.
*  Ke Tao, http://taubau.info/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.tal.model.query;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Aug 8, 2011
 */
public class QueryUtility {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public static String checkSpace(String label) {
		label = label.replace("'s", " ");
		label = label.replace("\"", " ");
		label = label.replace("!", " ");
		label = label.replace(",", " ");
		label = label.replace("'", " ");
		label = label.replace("?", " ");
		label = label.replace(".", " ");
		label = label.replace("-", " ");
		label = label.replace("–", " ");
		label = label.replace("(", "");
		label = label.replace(")", "");
		label = label.replace("&", " ");
		label = label.replace(":", " ");
		label = label.replaceAll("  ", " ");
		
		if(label.contains("^")) {
			return "";
		}

		if(label.contains(" ")) {
			return label.trim();
		} else {
			return label;
		}
	}
	
	public static String dbpediaURItoLabel(String dbpediaURI) {
		try {
			dbpediaURI = URLDecoder.decode(dbpediaURI, "UTF-8");
		} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();
		}
		
		dbpediaURI = dbpediaURI.replace("http://dbpedia.org/resource/", "");
		
		Pattern pattern = Pattern.compile("\\([^\\(\\)]*\\)");
		Matcher matcher = pattern.matcher(dbpediaURI);
		dbpediaURI = matcher.replaceAll("");
		dbpediaURI = dbpediaURI.replace("_", " ");
		dbpediaURI = dbpediaURI.trim();
		
		dbpediaURI = checkSpace(dbpediaURI);
		
		return dbpediaURI;
	}
	
	public static String dbpediaURItoLabel(String dbpediaURI, boolean whole) {
		if(whole) {
			if(dbpediaURI.contains(" ")) {
				dbpediaURI = "#1(" + dbpediaURItoLabel(dbpediaURI) + ")"; 
			}
			return dbpediaURI;
		} else {
			return dbpediaURItoLabel(dbpediaURI);
		}
	}
	
	public static Set<String> stopwords = new HashSet<String>();
	static{
		stopwords.add("the");
		stopwords.add("a");
		stopwords.add("of");
		stopwords.add("and");
		stopwords.add("or");
		stopwords.add("are");
		stopwords.add("were");
		stopwords.add("they");
		stopwords.add("he");
		stopwords.add("she");
		stopwords.add("it");
	}
	/**
	 * 
	 * @param term the term to check
	 * @return <code>true</code> if the term is considered as stopword, otherwise <code>false</code>
	 */
	public static boolean isStopword(String term){
		return stopwords.contains(term.toLowerCase());
	}
}
