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

import twitter4j.HashtagEntity;

/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 23, 2014
 */
public class Hashtag implements HashtagEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7515310209023465874L;
	
	int start = -1;
	int end = -1;
	String hashtag;
	
	public Hashtag(String hashtag) {
		this.hashtag = hashtag;
	}
	
	/* (non-Javadoc)
	 * @see twitter4j.HashtagEntity#getText()
	 */
	public String getText() {
		return hashtag;
	}

	/* (non-Javadoc)
	 * @see twitter4j.HashtagEntity#getStart()
	 */
	public int getStart() {
		return start;
	}

	/* (non-Javadoc)
	 * @see twitter4j.HashtagEntity#getEnd()
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
