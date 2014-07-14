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
 * A weighted term consists of a label and weight. The higher the weight the more important the term.
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @version created on Aug 10, 2011
 */
public class WeightedTerm {

	public String term = null;
	public Double weight = 0.0;
	
	/**
	 * @param term the term (label/word)
	 * @param weight the weight of the term
	 */
	public WeightedTerm(String term, Double weight) {
		super();
		this.term = term;
		this.weight = weight;
	}

	/** 
	 * Equality is just based on the term (not on the weight).
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj != null && term != null && obj instanceof WeightedTerm && term.equals(((WeightedTerm)obj).term));
	}

	/**
	 * The hashcode is just based on the term, i.e. it calls {@link String#hashCode()}. 
	 */
	@Override
	public int hashCode() {
		if(term != null){
			return term.hashCode();
		}
		return super.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return term + " (" + weight + ")";
	}
}
