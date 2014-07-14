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

import java.net.URL;
import java.util.Map;

import twitter4j.MediaEntity;

/**
 * 
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 23, 2014
 */
public class Media implements MediaEntity {

	private static final long serialVersionUID = -5420146287379858570L;

	/* (non-Javadoc)
	 * @see twitter4j.URLEntity#getDisplayURL()
	 */
	public String getDisplayURL() {
		return null;
	}

	/* (non-Javadoc)
	 * @see twitter4j.URLEntity#getStart()
	 */
	public int getStart() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.URLEntity#getEnd()
	 */
	public int getEnd() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.MediaEntity#getId()
	 */
	public long getId() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see twitter4j.MediaEntity#getSizes()
	 */
	public Map<Integer, Size> getSizes() {
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Testing code here
	}

	public String getExpandedURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMediaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMediaURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
