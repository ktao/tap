/*********************************************************
*  Copyright (c) 2013 by Web Information Systems (WIS) Group.
*  Ke Tao, http://ktao.nl/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.tal.model;

import twitter4j.UserMentionEntity;

/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 23, 2014
 */
public class UserMention implements UserMentionEntity {

	private static final long serialVersionUID = -2795611829894533443L;
	private String username;
	private long userId;
	
	public UserMention(String username) {
		this.username = username;
	}
	
	public UserMention(long userId) {
		this.userId = userId;
	}
	
	/* (non-Javadoc)
	 * @see twitter4j.UserMentionEntity#getName()
	 */
	public String getName() {
		return username;
	}
	
	public void setName(String username) {
		this.username = username;
	}

	/* (non-Javadoc)
	 * @see twitter4j.UserMentionEntity#getScreenName()
	 */
	public String getScreenName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.UserMentionEntity#getId()
	 */
	public long getId() {
		return userId;
	}
	
	public void setId(long userId) {
		this.userId = userId;
	}

	/* (non-Javadoc)
	 * @see twitter4j.UserMentionEntity#getStart()
	 */
	public int getStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.UserMentionEntity#getEnd()
	 */
	public int getEnd() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

}
