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

import java.sql.Timestamp;

/**
 * A topic mainly contains of an ID, title and query time.
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @version created on Aug 10, 2011
 */
public class Topic {

	/** the ID of the topic */
	public Integer id = null;

	/** some sort og public identifier of the topic */
	public String reference = null;
	
	/** category into which the topic is grouped (e.g. example topic (= trec_2011_microblog_example), trec topic (= trec_2011_microblog))*/
	public String topicGroup = null;
	
	/** the title of the topic */
	public String title = null;
	
	/** the timestamp when a query is issued, i.e. when interesting items about the topic should be retrieved */
	public Timestamp queryTime = null;
	
	/** the timestamp when a query is issued measured by means of a tweet ID*/
	public Long queryTweetTime = null;

	/**
	 * Standard constructor.
	 */
	public Topic() {
		super();
	}

	/**
	 * @param id
	 * @param reference
	 * @param topicGroup
	 * @param title
	 * @param queryTime
	 * @param queryTweetTime
	 */
	public Topic(Integer id, String reference, String topicGroup, String title,
			Timestamp queryTime, Long queryTweetTime) {
		super();
		this.id = id;
		this.reference = reference;
		this.topicGroup = topicGroup;
		this.title = title;
		this.queryTime = queryTime;
		this.queryTweetTime = queryTweetTime;
	}

	/**
	 * @param id
	 * @param reference
	 * @param title
	 * @param queryTweetTime
	 */
	public Topic(Integer id, String reference, String title, Long queryTweetTime) {
		super();
		this.id = id;
		this.reference = reference;
		this.title = title;
		this.queryTweetTime = queryTweetTime;
	}
	
	
	
}
