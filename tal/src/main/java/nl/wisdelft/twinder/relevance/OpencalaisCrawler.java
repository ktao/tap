/*********************************************************
*  Copyright (c) 2010 by Web Information Systems (WIS) Group.
*  Fabian Abel, http://fabianabel.de/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.relevance;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.wisdelft.twinder.io.JDBCUtility;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

/**
 * This class queries <a href="http://www.opencalais.com/">OpenCalais</a> to 
 * retrieve further semantic descriptions about news articles (and tweets).
 * 
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @author last edited by: $Author: fabian $
 * 
 * @version created on Nov 26, 2010
 * @version $Revision: 1.1 $ $Date: 2011-01-25 18:46:38 $
 */
public class OpencalaisCrawler {

	private static final String API_KEY_fabian = "y7v4n7epq2pbx7knwknftjx7";
	private static final String API_KEY_wisteam = "76eney9gxpg94hjkjp6smzuy";
	private static final String API_KEY_wisresearch = "cejxhk6wzfkfr6h8rapa7bjg";
	private static final String API_KEY_wismasters = "qdtbh8unduhmfb78z654p8gz";
	private static final String API_KEY_ke = "2bdxbyvqw7th63w2gw36ppkt";
	private static final String API_KEY_ke_2 = "zh8vsyvz2cyagp2vk83gq3gq";
	private static final String API_KEY_semantics1 = "xahvanezgjxbkgxhhau9c245";//lydia-semantics
	private static final String API_KEY_semantics2 = "eebc4d5kvh5bk2yhaqepxmfg";//p2psuv
	private static final String API_KEY_semantics3 = "qnfr9zrxnjnxgh87gzdy8wvm";//andy19881019
	private static final String API_KEY_semantics4 = "ft4s6mu6gd9e92w5qdcd4mt5";//fabelwis
	private static final String API_KEY_semantics5 = "4ca9zk5hr677dgfcrr5gxhay";//andy.taoke.yahoo
	
	private static final String API_KEY_hotmail_taskforce_1 = "5hxggdfct3bh6ny8mex335sc";//k.tao.tudelft.1.hotmail
	private static final String API_KEY_hotmail_taskforce_2 = "2erd5bgvzw7zacv6dr3tvptp";//k.tao.tudelft.2.hotmail
	private static final String API_KEY_hotmail_taskforce_3 = "8cnhasqc4gytrc4kxkh79njf";//k.tao.tudelft.3.hotmail
	private static final String API_KEY_hotmail_taskforce_4 = "wrhbfjybzp64zyuhnbhpkstc";//k.tao.tudelft.4.hotmail
	private static final String API_KEY_hotmail_taskforce_5 = "9tdft3kdx2axbxg64hqr2hjm";//k.tao.tudelft.5.hotmail
	private static final String API_KEY_hotmail_taskforce_6 = "vhgupeycctx64yswcyb545xz";//k.tao.tudelft.6.hotmail
	
	private static List<String> API_KEYS = new ArrayList<String>();
	static{
		API_KEYS.add(API_KEY_fabian);
		API_KEYS.add(API_KEY_wisteam);
		API_KEYS.add(API_KEY_wisresearch);
		API_KEYS.add(API_KEY_wismasters);
		API_KEYS.add(API_KEY_ke);
		API_KEYS.add(API_KEY_ke_2);
		API_KEYS.add(API_KEY_semantics1);
		API_KEYS.add(API_KEY_semantics2);
		API_KEYS.add(API_KEY_semantics3);
		API_KEYS.add(API_KEY_semantics4);
		API_KEYS.add(API_KEY_semantics5);
	}
	
//	private static List<String> API_KEYS = new ArrayList<String>();
//	static {
//		API_KEYS.add(API_KEY_hotmail_taskforce_1);
//		API_KEYS.add(API_KEY_hotmail_taskforce_2);
//		API_KEYS.add(API_KEY_hotmail_taskforce_3);
//		API_KEYS.add(API_KEY_hotmail_taskforce_4);
//		API_KEYS.add(API_KEY_hotmail_taskforce_5);
//		API_KEYS.add(API_KEY_hotmail_taskforce_6);
//	}
	
	public static String API_KEY = API_KEY_fabian;
//	public static String API_KEY = API_KEY_hotmail_taskforce_1;
	
	private static void switchAPIKey(){
		int index = API_KEYS.indexOf(API_KEY) + 1;
		if(index < API_KEYS.size()){
			API_KEY = API_KEYS.get(index);
		}else{
			API_KEY = API_KEYS.get(0);
		}
	}
	
	public static String switchAPIKey(String oldKey){
		int index = API_KEYS.indexOf(oldKey) + 1;
		if(index < API_KEYS.size() && index > 0){
			return API_KEYS.get(index);
		}else{
			return API_KEYS.get(0);
		}
	}
	
	/**
	 * Processing the tweets with open calais. Which tweets? Those tweets were created
	 * after Jan 3rd 2011
	 * 
	 * @param args the program arguments from the main {@link OpencalaisCrawler#main(String[])}
	 */
	public static void processTweets(String[] args) {
		int total = 120000;
		int single = 10000;
		int start = 0;
		if(args != null && args.length == 2){
			total = Integer.parseInt(args[0]);
			single = Integer.parseInt(args[1]);
			do {
				String query = null;
				String lastAPIKey = null;
				ExecutorService executer = Executors.newFixedThreadPool(5); //use more threads when you have enough memory...
				System.out.println( "*****************************\n" +
									"* Processing: " + start + " to " + (start + single) + "\n" +
									"*****************************");
				query = "SELECT id, content "
						+ "FROM tweets_judged "
						+ "LIMIT " + start + ", " + single;
						
				lastAPIKey = switchAPIKey(lastAPIKey);
				executer.execute(new OpencalaisCrawlerTwitterThread(query, lastAPIKey));
				start += single;
			} while (start < total + single);
			
		}else{
			System.out.println("Specify userId sample, minimum creation date and an OpenCalais API key:\n" +
					"java -jar [jarfile] total single\n" +
					"\n  total\t total number of tweets to be processed" +
					"\n  single\t the amount of tweets for each thread");
		}
	}
	
	public static void main(String[] args) {
		processTweets(args);
	}
}
