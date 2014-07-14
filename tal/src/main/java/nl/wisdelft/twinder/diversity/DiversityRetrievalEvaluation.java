/**
 * 
 */
package nl.wisdelft.twinder.diversity;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import nl.wisdelft.twinder.io.JDBCDataSource;
import nl.wisdelft.twinder.tal.model.evaluation.NDEvalResult;

/**
 * @author ktao
 * Last action: Jun 9th, 2014
 * However, this will left to the later work.
 */
public class DiversityRetrievalEvaluation {

	/** The dynamic datasource that contains the experimental data */
	private static JDBCDataSource ds = new JDBCDataSource("jdbc:mysql://apsthree.st.ewi.tudelft.nl/twinder", "trec", "trec@w1s");
	
	private static Evaluator eval = new Evaluator("/Volumes/ZTZ-99/profession.workspace/airs2013/qrel/qrel-all", 
			"/Volumes/ZTZ-99/index/trec2013_lucene_index");
	
	/**
	 * Given a topic, find all the related concepts and test, for each concept, whether the effectiveness will increase.
	 * @return
	 */
	public static ArrayList<String> expandWithOneConcept(int topicId, String originQuery) {
		ArrayList<String> cList = new ArrayList<String>();
		
		File rfile = eval.search(Integer.toString(topicId), "twitter-diversity", originQuery);
		double baseline = eval.evalute(rfile).anDCG[2];
		System.out.println("The alpha-nDCG at 20 for original query: " + baseline);
		
		ResultSet concepts = ds.executeQuerySingleConnection("SELECT wEntryId, wEntryTitle "
				+ "FROM airs2013_direct_linked_concept_wm "
				+ "WHERE topicId = " + topicId);
		
		try {
			double score = 0.0;
			while (concepts.next()) {
				String concept = concepts.getString("wEntryTitle");
				rfile = eval.search(Integer.toString(topicId), "twitter-diversity", originQuery + " " + concept);
				score = eval.evalute(rfile).anDCG[2];
				if (score > baseline) {
					cList.add(concept);
					System.out.println("The concept" + concept +  " improves alpha-nDCG at 20 to " + score + "; difference " + (score - baseline));
					System.out.println(concept);
				} else {
					System.out.println("Not improving: " + concept);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return cList;
	}
	
	/**
	 * Question: Which measure should we use? - alpha-nDCG@20
	 * @param args
	 */
	public static void main(String[] args) {
		// expand the original query with one concept, output the list of concepts that improve the retrieval effectiveness (alpha-nDCG)
		// sigir2014_direct_linked_concept_wm
		expandWithOneConcept(3, "senkaku dispute");
		
		// expand to an optimal query using greedy algorithm
	}

}
