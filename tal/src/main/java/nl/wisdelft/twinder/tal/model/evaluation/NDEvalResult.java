/**
 * 
 */
package nl.wisdelft.twinder.tal.model.evaluation;

/**
 * @author ktao
 *
 */
public class NDEvalResult {
	
	/** runtag, usually the strategy name, probably also involving the candidate terms */
	public String runtag;
	
	/** topicid */
	public int tid;
	
	/** ERR-IA@5,@10,@20 */
	public double[] ERR_IA;
	
	/** nERR-IA@5,@10,@20 */
	public double[] nERR_IA;
	
	/** alpha-DCG@5,@10,@20 */
	public double[] aDCG;
	
	/** alpha-nDCG@5,@10,@20 */
	public double[] anDCG;
	
	/** NRBP */
	public double nrbp;
	
	/** nNRBP */
	public double nnrbp;
	
	/** P-IA */
	public double[] pIA;
	
	/** s-trec */
	public double[] strec;
	
	/**
	 * runid,topic,
	 * ERR-IA@5,ERR-IA@10,ERR-IA@20
	 * nERR-IA@5,nERR-IA@10,nERR-IA@20
	 * alpha-DCG@5,alpha-DCG@10,alpha-DCG@20
	 * alpha-nDCG@5,alpha-nDCG@10,alpha-nDCG@20
	 * NRBP,nNRBP
	 * P-IA@5,P-IA@10,P-IA@20
	 * strec@5,strec@10,strec@20
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
