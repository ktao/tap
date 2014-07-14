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

import java.util.ArrayList;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * A query profile is a set of weighted terms/labels.
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @version created on Aug 10, 2011
 */
@SuppressWarnings("serial")
public class QueryProfile extends ArrayList<WeightedTerm> {

	/**
	 * Inserts the weighted term into the profile. If the term
	 * is already contained in the profile then the weight will
	 * be increased, i.e. weight_new = weight_old + weight_wt
	 * 
	 * @param wt the weighted term to add or merge in
	 */
	public void mergeIn(WeightedTerm wt){
		if(this.contains(wt)){
			this.get(this.indexOf(wt)).weight += wt.weight;
		}else{
			this.add(wt);
		}
	}
	
	
	/**
	 * 1-norm: normalize so that sum of weights equals 1
	 */
	public void normalize(){
		Double sumOfWeights = 0.0;
		for(WeightedTerm wt: this){
			sumOfWeights += wt.weight;
		}
		if(sumOfWeights > 0){
			for(int i=0; i<this.size(); i++){
				this.get(i).weight = this.get(i).weight / sumOfWeights; 
			}
		}
	}
	
	/**
	 * Give a query extended with semantics
	 * @return
	 */
	public BooleanQuery toLuceneQuery() {
		BooleanQuery query = new BooleanQuery();
		
		for (WeightedTerm wt : this) {
			TermQuery tq = new TermQuery(new Term("contents", wt.term));
			tq.setBoost(wt.weight.floatValue());
			query.add(tq, Occur.SHOULD);
		}
		
		return query;
	}


	/**
	 * 
	 * @return
	 */
	public String toLuceneWeightQuery() {
		String query = "";
		for (WeightedTerm wt : this) {
			TermQuery tq = new TermQuery(new Term("contents", wt.term));
			tq.setBoost(wt.weight.floatValue());
			query += "\"" + wt.term + "\"^" + wt.weight + " ";
		}
		return query.trim();
	}
}
