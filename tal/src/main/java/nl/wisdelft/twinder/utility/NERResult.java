/*********************************************************
*  Copyright (c) 2013 by Web Information Systems (WIS) Group.
*  Ke Tao, http://ktao.nl/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.utility;

import java.util.ArrayList;

import nl.wisdelft.twinder.tal.model.SemanticEntity;
import nl.wisdelft.twinder.tal.model.SemanticTopic;

/**
 * The result in unified form that will be returned by Named Recognition Services.
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 24, 2014
 */
public class NERResult {
	
	/** The entities recognized by the service */
	private ArrayList<SemanticEntity> entities;
	
	/** The topic recognized by the service by e.g. OpenCalais */
	private ArrayList<SemanticTopic> topics;
	
	public NERResult() {
		
	}
	
	public void addEntity(SemanticEntity entity) {
		if (entities == null)
			entities = new ArrayList<SemanticEntity>();
		entities.add(entity);
	}
	
	public void addTopic(SemanticTopic topic) {
		if (topics == null)
			topics = new ArrayList<SemanticTopic>();
		topics.add(topic);
	}

	public ArrayList<SemanticEntity> getEntities() {
		return entities;
	}

	public ArrayList<SemanticTopic> getTopics() {
		return topics;
	}
}
