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

/**
 * Merges the results of two given query generation strategies.
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @version created on Aug 10, 2011
 */
public class QueryGeneration_Merge implements QueryGeneration {

	/** query generation (= topic modeling) strategy A */
	public QueryGeneration strategyA = null;
	
	/** influence of query generation (= topic modeling) strategy A */
	public double influenceOfA = 0.5;
	
	/** query generation (= topic modeling) strategy B */
	public QueryGeneration strategyB = null;
	/** influence of query generation (= topic modeling) strategy B */
	public double influenceOfB = 0.5;
	
	
	/**
	 * @param strategyA
	 * @param influenceOfA
	 * @param strategyB
	 * @param influenceOfB
	 */
	public QueryGeneration_Merge(QueryGeneration strategyA,
			double influenceOfA, QueryGeneration strategyB, double influenceOfB) {
		super();
		this.strategyA = strategyA;
		this.influenceOfA = influenceOfA;
		this.strategyB = strategyB;
		this.influenceOfB = influenceOfB;
	}

	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#generateQuery(org.wis.trec.microblog.model.Topic)
	 */
	public QueryProfile generateQuery(Topic topic) {
		QueryProfile profile = new QueryProfile();
		
		QueryProfile profileX = strategyA.generateQuery(topic);
		for(WeightedTerm wt: profileX){
			wt.weight = wt.weight * influenceOfA;
			profile.add(wt);
		}
		profileX = strategyB.generateQuery(topic);
		for(WeightedTerm wt: profileX){
			wt.weight = wt.weight * influenceOfB;
			profile.mergeIn(wt);
		}
		//normalize:
		profile.normalize();
		
		return profile;
	}

	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#getName()
	 */
	public String getName() {
		return strategyA.getName() + "_influence-" + influenceOfA + "_JOIN_" + strategyB.getName() + "_influence-" + influenceOfB;
	}

}
