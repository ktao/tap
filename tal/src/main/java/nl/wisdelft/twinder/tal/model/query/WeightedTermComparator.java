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

import java.util.Comparator;

/**
 * Compares the weights of two weighted terms. It does not look at the term, 
 * but just calls {@link Double#compareTo(Double)} and multiplies result with -1, 
 * i.e. term with highest weight will be at first position. 
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @author last edited by: $Author: $
 * 
 * @version created on Aug 10, 2011
 * @version $Revision: $ $Date: $
 */
public class WeightedTermComparator implements Comparator<WeightedTerm> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(WeightedTerm arg0, WeightedTerm arg1) {
		if(arg0.weight != null){
			return arg0.weight.compareTo(arg1.weight) * -1;
		}else if(arg1.weight != null){
			return arg1.weight.compareTo(arg0.weight) * -1;
		}
		return 0;
	}

}
