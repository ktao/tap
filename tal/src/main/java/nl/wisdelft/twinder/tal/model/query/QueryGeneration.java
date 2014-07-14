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

import nl.wisdelft.twinder.tal.model.query.QueryProfile;
import nl.wisdelft.twinder.tal.model.query.Topic;

/**
 * Generating a query for a given topic = creating a (short) profile for the topic. 
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @version created on Aug 10, 2011
 */
public interface QueryGeneration {

	/** 
	 * Generates a query profile for a given topic.
	 *  
	 * @param topic the topic
	 * @return the query profile of the topic
	 */
	public QueryProfile generateQuery(Topic topic);
	
	/**
	 * @return the name/identifier of the strategy
	 */
	public String getName();
}
