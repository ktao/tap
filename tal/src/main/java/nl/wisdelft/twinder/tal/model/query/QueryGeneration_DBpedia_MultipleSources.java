/*********************************************************
*  Copyright (c) 2010 by Web Information Systems (WIS) Group.
*  Fabian Abel, http://fabianabel.de/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.tal.model.query;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.util.HashMap;

import nl.wisdelft.twinder.io.JDBCUtility;
import nl.wisdelft.twinder.utility.AlchemyAnnotator;
import nl.wisdelft.twinder.utility.AlchemyAnnotator.ExtractedEntity;
import nl.wisdelft.twinder.utility.SpotlightAnnotator;
import nl.wisdelft.twinder.utility.SpotlightAnnotator.SpotlightEntity;
import nl.wisdelft.twinder.utility.ZemantaAnnotator;

/**
 * Generates a query profile by extracting the labels of DBpedia entities 
 * that can be extracted from the title of a topic.
 * Some features:
 * - evidence that a DBpedia entity is relevant for the topic is based on
 *   NER provided (1) DBpedia spotlight, (2) Alchemy and (3) Zemanta.
 * 
 * - query terms are weighted based on their inferred relevance/certainty for the topic, 
 *   e.g. if an entity was detected by all three different services then it will get a higher weight 
 *   than an entity that was just detected by one service. 
 *   
 * 
 * Note: this strategy queries the database by using {@link JDBCUtility}, i.e. modify database properties accordingly (see 
 * https://svn.st.ewi.tudelft.nl/wis/persweb/um-twitter-news/src/org/wis/twitter/io/twitterdb.properties)
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @version created on Aug 10, 2011
 * @see {@link SpotlightAnnotator}, {@link AlchemyAnnotator}, {@link ZemantaAnnotator} 
 */
public class QueryGeneration_DBpedia_MultipleSources implements QueryGeneration {

	public QueryProfile generateQueryInRealTime(Topic topic) {
		QueryProfile profile = new QueryProfile();
		
		// Alchemy
		for (AlchemyAnnotator.ExtractedEntity e : AlchemyAnnotator.extractEntities(topic.title)) {
			if (e.name != null) {
				profile.add(new WeightedTerm(e.name, 1.0));
			}
		}
		
		// Zemanta
		for (ExtractedEntity ez : ZemantaAnnotator.extractEntities(topic.title)) {
			if (ez.name != null) {
				profile.add(new WeightedTerm(ez.name, 1.0));
			}
		}
		
		// DBpedia
		HashMap<String, SpotlightEntity> entities = SpotlightAnnotator.annotate(topic.title, 0.2, 20);
		for (String name : entities.keySet()) {
			WeightedTerm wt = new WeightedTerm(name, 1.0);
			try {
				wt.term = URLDecoder.decode(wt.term, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			wt.term = QueryUtility.checkSpace(wt.term);
			wt.term = wt.term.replaceAll("  ", " "); //replace double spaces
			
			//DBpedia spotlight seems to be quite noisy, i.e. here we do some tweaking of the weights:
			if(!entities.get(name).text.contains(" ")){//if a single word is annotated then we decrease the weight:
				wt.weight = 0.5;
			}
			
			profile.mergeIn(wt);
		}
		
		//if no entities are extracted then we simply add the title of the topic as query term:
		if(profile.isEmpty()){
			WeightedTerm wt = new WeightedTerm(topic.title, 1.0);
			wt.term = QueryUtility.checkSpace(wt.term);
			wt.term = wt.term.replaceAll("  ", " "); //replace double spaces
			profile.add(wt);
		}
			
		//for labels that consists of multiple words, we also insert the individual words as query terms 
		//(however, we weight them just with 25% of their original weight)
		QueryProfile newTerms = new QueryProfile();
		String label = null;
		WeightedTerm wtNew = null;
		for(WeightedTerm wt: profile){
			if(wt.term.contains(" ")){
				label = wt.term;
				
				while(label.contains(" ")){
					wtNew = new WeightedTerm(label.substring(0, label.indexOf(" ")), 0.25);
					if(!QueryUtility.isStopword(wtNew.term)){
						newTerms.mergeIn(wtNew);
					}
					label = label.substring(label.indexOf(" ") + 1);
				}
				newTerms.mergeIn(new WeightedTerm(label, 0.25));
			}
		}
		for(WeightedTerm wt: newTerms){
			profile.mergeIn(wt);
		}
		
		// do not normalize so that sum of weights equals 1 (1-norm):
		// profile.normalize();
		
		return profile;
	}
	
	public QueryProfile generateQueryInRealTime(String query) {
		QueryProfile profile = new QueryProfile();
		
		// Alchemy
		for (AlchemyAnnotator.ExtractedEntity e : AlchemyAnnotator.extractEntities(query)) {
			if (e.name != null) {
				profile.add(new WeightedTerm(e.name, 1.0));
			}
		}
		
		// Zemanta
		for (ExtractedEntity ez : ZemantaAnnotator.extractEntities(query)) {
			if (ez.name != null) {
				profile.add(new WeightedTerm(ez.name, 1.0));
			}
		}
		
		// DBpedia
		HashMap<String, SpotlightEntity> entities = SpotlightAnnotator.annotate(query, 0.2, 20);
		for (String name : entities.keySet()) {
			WeightedTerm wt = new WeightedTerm(name, 1.0);
			try {
				wt.term = URLDecoder.decode(wt.term, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			wt.term = QueryUtility.checkSpace(wt.term);
			wt.term = wt.term.replaceAll("  ", " "); //replace double spaces
			
			//DBpedia spotlight seems to be quite noisy, i.e. here we do some tweaking of the weights:
			if(!entities.get(name).text.contains(" ")){//if a single word is annotated then we decrease the weight:
				wt.weight = 0.5;
			}
			
			profile.mergeIn(wt);
		}
		
		//if no entities are extracted then we simply add the title of the topic as query term:
		if(profile.isEmpty()){
			WeightedTerm wt = new WeightedTerm(query, 1.0);
			wt.term = QueryUtility.checkSpace(wt.term);
			wt.term = wt.term.replaceAll("  ", " "); //replace double spaces
			profile.add(wt);
		}
			
		//for labels that consists of multiple words, we also insert the individual words as query terms 
		//(however, we weight them just with 25% of their original weight)
		QueryProfile newTerms = new QueryProfile();
		String label = null;
		WeightedTerm wtNew = null;
		for(WeightedTerm wt: profile){
			if(wt.term.contains(" ")){
				label = wt.term;
				
				while(label.contains(" ")){
					wtNew = new WeightedTerm(label.substring(0, label.indexOf(" ")), 0.25);
					if(!QueryUtility.isStopword(wtNew.term)){
						newTerms.mergeIn(wtNew);
					}
					label = label.substring(label.indexOf(" ") + 1);
				}
				newTerms.mergeIn(new WeightedTerm(label, 0.25));
			}
		}
		for(WeightedTerm wt: newTerms){
			profile.mergeIn(wt);
		}
		
		// do not normalize so that sum of weights equals 1 (1-norm):
		// profile.normalize();
		
		return profile;
	}
	
	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#generateQuery(org.wis.trec.microblog.model.Topic)
	 */
	public QueryProfile generateQuery(Topic topic) {
		QueryProfile profile = new QueryProfile();
		
		//Alchemy:
		ResultSet dbpediaEntries = JDBCUtility.executeQuerySingleConnection("SELECT distinct name, score " +
				"FROM dbpediaEntityInTopic_alchemy where name is not NULL AND topicId = " + topic.id);
		try{
			if(dbpediaEntries != null){
				while(dbpediaEntries.next()){
					profile.add(new WeightedTerm(dbpediaEntries.getString("name"), 1.0));
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//Zemanta:
		dbpediaEntries = JDBCUtility.executeQuerySingleConnection("SELECT distinct name, score " +
				"FROM dbpediaEntityInTopic_zemanta where name is not NULL AND topicId = " + topic.id);
		try{
			if(dbpediaEntries != null){
				while(dbpediaEntries.next()){
					profile.mergeIn(new WeightedTerm(dbpediaEntries.getString("name"), 1.0));
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//DBpedia spotlight:
		dbpediaEntries = JDBCUtility.executeQuerySingleConnection("SELECT distinct annotatedText, replace(replace(replace(replace(DBpediaURI, \"http://dbpedia.org/resource/\", \"\"), \"_\", \" \"), \"%28\", \" \"), \"%29\", \" \") as name, score " +
				"FROM dbpediaEntityInTopic where DBpediaURI is not NULL AND topicId = " + topic.id);
		try{
			WeightedTerm wt = null;
			if(dbpediaEntries != null){
				while(dbpediaEntries.next()){
					wt = new WeightedTerm(dbpediaEntries.getString("name"), 1.0);
					wt.term = URLDecoder.decode(wt.term, "UTF-8");
					wt.term = QueryUtility.checkSpace(wt.term);
					wt.term = wt.term.replaceAll("  ", " "); //replace double spaces
					//DBpedia spotlight seems to be quite noisy, i.e. here we do some tweaking of the weights:
					if(!dbpediaEntries.getString("annotatedText").contains(" ")){//if a single word is annotated then we decrease the weight:
						wt.weight = 0.5;
					}
					profile.mergeIn(wt);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		//if no entities are extracted then we simply add the title of the topic as query term:
		if(profile.isEmpty()){
			WeightedTerm wt = new WeightedTerm(topic.title, 1.0);
			wt.term = QueryUtility.checkSpace(wt.term);
			wt.term = wt.term.replaceAll("  ", " "); //replace double spaces
			profile.add(wt);
		}
			
		//for labels that consists of multiple words, we also insert the individual words as query terms 
		//(however, we weight them just with 25% of their original weight)
		QueryProfile newTerms = new QueryProfile();
		String label = null;
		WeightedTerm wtNew = null;
		for(WeightedTerm wt: profile){
			if(wt.term.contains(" ")){
				label = wt.term;
				
				while(label.contains(" ")){
					wtNew = new WeightedTerm(label.substring(0, label.indexOf(" ")), 0.25);
					if(!QueryUtility.isStopword(wtNew.term)){
						newTerms.mergeIn(wtNew);
					}
					label = label.substring(label.indexOf(" ") + 1);
				}
				newTerms.mergeIn(new WeightedTerm(label, 0.25));
			}
		}
		for(WeightedTerm wt: newTerms){
			profile.mergeIn(wt);
		}
		
		//normalize so that sum of weights equals 1 (1-norm):
		profile.normalize();
		
		return profile;
	}

	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#getName()
	 */
	public String getName() {
		return "dbpedia-multiple-evidence";
	}

	public static void main(String[] args) {
		// test
		QueryGeneration_DBpedia_MultipleSources qdm = new QueryGeneration_DBpedia_MultipleSources();
		Topic t = new Topic(1, "MB001", "BBC World Service staff cuts", 34952194402811904L);
		
		System.out.println(qdm.generateQueryInRealTime(t).toLuceneWeightQuery());
	}
}
