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

import java.sql.ResultSet;
import java.util.Collections;

import nl.wisdelft.twinder.io.JDBCUtility;

/**
 * Query generation strategy that takes the top k entities from the top n 
 * query results, which are returned when using a topic-based query profile
 * ({@link QueryGeneration_DBpedia_MultipleSources}), while the index is generated from news.
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @version created on Aug 10, 2011
 */
public class QueryGeneration_DBpedia_Expanded_NewsDescription implements
		QueryGeneration {
	
	/** the top K entities that should be used as query, i.e.:
	 *  (i) when issueing a topic-based query (genrated e.g. by @link QueryGeneration_DBpedia_MultipleSources}) we get a set of news N1 as results 
	 *  (ii) given N1, we order the entities mentioned in news in N1 according to their occurrence frequency 
	 *  (iii) the top k entities are then used to generate the query profile*/
	public int topKEntities = 20;
	
	/** the top N news that should be considered, i.e.:
	 * (i) when issueing a topic-based query (genrated e.g. by @link QueryGeneration_DBpedia_MultipleSources}) we get a ranking of news N1 as result 
	 * (ii) given N1, we will consider only entities mentioned in the top N result news (of those news that are in the time range and mention an entity) 
	 *  */
	public int topNResults = 15;
	
	/** the number of days within which we consider a news as relevant for the topic, i.e.
	 * given the topic X, a news n and 4 as number of relevant days then t is timely relevant if: |X.querytime - t.creationTime| <= 4 days 
	 */
	public int numberOfRelevantDays = 4;
	
	/** the ID of the run that produced the search results in the topic-based query run, i.e. one of the 
	 * values in the field "indriResult.id" of the database.
	 */
	public Integer run = null;

	/**
	 * @param run
	 */
	public QueryGeneration_DBpedia_Expanded_NewsDescription(Integer run) {
		super();
		this.run = run;
	}

	/**
	 * @param topKEntities
	 * @param topNResults
	 * @param numberOfRelevantDays
	 * @param run
	 */
	public QueryGeneration_DBpedia_Expanded_NewsDescription(int topKEntities,
			int topNResults, int numberOfRelevantDays, Integer run) {
		super();
		this.topKEntities = topKEntities;
		this.topNResults = topNResults;
		this.numberOfRelevantDays = numberOfRelevantDays;
		this.run = run;
	}

	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#generateQuery(org.wis.trec.microblog.model.Topic)
	 */
	public QueryProfile generateQuery(Topic topic) {
		QueryProfile profile = new QueryProfile();
		
		//Spotlight:
		ResultSet entities = JDBCUtility.executeQuerySingleConnection("SELECT annotatedText, DBpediaURI, count(*) n from dbpediaEntityInNewsDescription a, " +
				"(SELECT d.newsId from indriResultNewsEntry i, topic, dbpediaEntityInNewsDescription d where topicId = " + topic.id + 
				" AND topic.id = i.topicId AND resultId  = " + this.run.intValue() + " AND i.newsId = d.newsId AND " +
				" UNIX_TIMESTAMP(d.creationTime) < (UNIX_TIMESTAMP(topic.queryTime) + (" + numberOfRelevantDays + " * 24 * 60 * 60)) AND " + 
				" UNIX_TIMESTAMP(d.creationTime) > (UNIX_TIMESTAMP(topic.queryTime) - (" + numberOfRelevantDays + "  * 24 * 60 * 60)) " +
				" order by rank asc limit " + topNResults + " ) as t WHERE t.newsId = a.newsId group by DBpediaURI order by n desc limit " + topKEntities);
		try{
			WeightedTerm wt = null;
			while(entities.next()){
				wt = new WeightedTerm(entities.getString("DBpediaURI"), entities.getDouble("n"));
				wt.term = QueryUtility.dbpediaURItoLabel(wt.term);
				
				//DBpedia spotlight seems to be quite noisy, i.e. here we do some tweaking of the weights:
				if(!entities.getString("annotatedText").contains(" ")){//if a single word is annotated then we decrease the weight:
					wt.weight = wt.weight * 0.5;
				}
				profile.mergeIn(wt);
			}
		}catch (Exception e) {
			e.printStackTrace();
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
					wtNew = new WeightedTerm(label.substring(0, label.indexOf(" ")), wt.weight * 0.25);
					if(!QueryUtility.isStopword(wtNew.term)){
						newTerms.mergeIn(wtNew);
					}
					label = label.substring(label.indexOf(" ") + 1);
				}
				newTerms.mergeIn(new WeightedTerm(label, wt.weight * 0.25));
			}
		}
		for(WeightedTerm wt: newTerms){
			profile.mergeIn(wt);
		}
		
		Collections.sort(profile, new WeightedTermComparator());
		if(profile.size() <= topKEntities){
			//normalize:
			profile.normalize();
			return profile;
		}else{
			QueryProfile topkProfile = new QueryProfile();
			for(int i = 0; i < topKEntities && i < profile.size(); i++){
				topkProfile.add(profile.get(i));
			}
			//normalize:
			topkProfile.normalize();
			return topkProfile;
		}
	}

	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#getName()
	 */
	public String getName() {
		return "dbpedia-expanded-news-description_" + topNResults + "-results_" + topKEntities + "-entities_run-" + run.intValue();
	}

}
