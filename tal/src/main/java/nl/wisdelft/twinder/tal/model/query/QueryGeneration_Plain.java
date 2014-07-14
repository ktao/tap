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


/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Oct 21, 2013
 */
public class QueryGeneration_Plain implements QueryGeneration {

	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#generateQuery(org.wis.trec.microblog.model.Topic)
	 */
	public QueryProfile generateQuery(Topic topic) {
		QueryProfile qp = new QueryProfile();
		qp.add(new WeightedTerm(topic.title, 1.0));
		return qp;
	}

	/* (non-Javadoc)
	 * @see org.wis.trec.microblog.query.QueryGeneration#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return "Plain";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
