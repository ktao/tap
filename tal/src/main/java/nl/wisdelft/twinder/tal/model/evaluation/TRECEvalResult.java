/**
 * 
 */
package nl.wisdelft.twinder.tal.model.evaluation;

/**
 * @author ktao
 *
 */
public class TRECEvalResult {
	
	/** runtag, usually the strategy name, probably also involving the candidate terms */
	public String runtag;
	
	/** topicid */
	public int tid;
	
	/** number of returns */
	public int num_ret;
	
	/** number of relevant */
	public int num_rel;
	
	/** number of relevant returns */
	public int num_rel_ret;
	
	/** map */
	public double map;
	
	/** gm_map */
	public double gm_map;
	
	/** Rprec */
	public double rprec;
	
	/** bpref */
	public double bpref;
	
	/** recip_rank */
	public double recip_rank;
	
	/** iprec_at_recall */
	public double[] iprec_at_recall;
	
	/** precision at k */
	public double[] iprec_at_k;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
