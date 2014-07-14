/**
 * 
 */
package nl.wisdelft.twinder.diversity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import nl.wisdelft.twinder.lucene.Searcher;
import nl.wisdelft.twinder.tal.model.evaluation.NDEvalResult;
import nl.wisdelft.twinder.tal.model.evaluation.TRECEvalResult;
import nl.wisdelft.twinder.utility.PropertyReader;

/**
 * @author ktao
 *
 */
public class Evaluator {
	
	/** The relevance judgment file */
	private File qrel;
	
	/** The search from where you retrieve the documents */
	private Searcher searcher;
	
	/** The location where the ndeval program is stored */
	private String ndeval;
	
	/** The location where the ndeval program is stored */
	private String treceval;
	
	/** The tmp folder where the result is stored */
	private static String RTMPDIR = PropertyReader.getString("tal.diversity.ndeval.tmp");
	
	public Evaluator(String qrelFilename, String indexDir) {
		this.qrel = new File(qrelFilename);
		this.searcher = new Searcher(indexDir);
		this.ndeval = PropertyReader.getString("tal.diversity.ndeval");
		this.treceval = PropertyReader.getString("tal.diversity.treceval");
	}
	
	/**
	 * The top 1000 results items will be written to the temporary file.
	 * @param query The query in String that will be submit to Lucene index.
	 * @return The file that stores the result
	 */
	public File search(String tid, String runtag, String query) {
		Date time = new Date();
		SimpleDateFormat df = new SimpleDateFormat("YYmmddHHmmssSSS");
		File rfile = new File(RTMPDIR + tid + "-" + runtag + "-" + df.format(time) + ".result");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(rfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Document tweet = null;
		int rank = 0;
		for (ScoreDoc doc : searcher.search(query, 1000).scoreDocs) {
			rank++;
			try {
				tweet = searcher.getDocument(doc.doc);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error at:" + rank + " " + tid + " " + runtag);
				continue;
			}
			try {
				bw.write(tid + " Q0 " + tweet.get("id") + " " + rank + " " + doc.score + " " + runtag + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error at:" + rank + " " + tid + " " + runtag);
				continue;
			}
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return rfile;
	}
	
	/**
	 * Given the result file, evaluate the result with the qrel, and return a standard result
	 * object 
	 * @param resultFilename The file in which the result for the query is stored.
	 */
	public NDEvalResult evalute(File rfile) {
		try {
			Process p = Runtime.getRuntime().exec(ndeval + " " 
												+ qrel.getAbsolutePath() + " " 
												+ rfile.getAbsolutePath());
			
			p.waitFor();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line=reader.readLine(); // 1st line
            line = reader.readLine(); // 2nd line
            
            String[] m = line.split(",");
            NDEvalResult res = new NDEvalResult();
            res.ERR_IA = new double[]{
            		Double.parseDouble(m[2]), 
            		Double.parseDouble(m[3]), 
            		Double.parseDouble(m[4])};
            
            res.nERR_IA = new double[]{
            		Double.parseDouble(m[5]), 
            		Double.parseDouble(m[6]), 
            		Double.parseDouble(m[7])};
            
            res.aDCG = new double[]{
            		Double.parseDouble(m[8]), 
            		Double.parseDouble(m[9]), 
            		Double.parseDouble(m[10])};
            
            res.anDCG = new double[]{
            		Double.parseDouble(m[11]), 
            		Double.parseDouble(m[12]), 
            		Double.parseDouble(m[13])};
            
            res.nrbp = Double.parseDouble(m[14]);
            res.nnrbp = Double.parseDouble(m[15]);
            
            res.pIA = new double[]{
            		Double.parseDouble(m[16]), 
            		Double.parseDouble(m[17]), 
            		Double.parseDouble(m[18])};
            
            res.strec = new double[]{
            		Double.parseDouble(m[19]), 
            		Double.parseDouble(m[20]), 
            		Double.parseDouble(m[21])};
            
            return res;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("The output error. Please check.");
		}
		return null;
	}
	
	/**
	 * Given the result file, evaluate the result with the qrel, and return a standard result
	 * object 
	 * @param resultFilename The file in which the result for the query is stored.
	 */
	public TRECEvalResult TRECEvalute(File rfile) {
		try {
			Process p = Runtime.getRuntime().exec(treceval + " " 
												+ qrel.getAbsolutePath() + " " 
												+ rfile.getAbsolutePath());
			
			p.waitFor();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            TRECEvalResult res = new TRECEvalResult();
            
            String line = reader.readLine(); // 1st line runid
            
            res.runtag = line.substring(line.indexOf("\tall\t") + 5).trim(); 
            
            line = reader.readLine(); // 2nd line - num_q; ignored;
            
            line = reader.readLine(); // 3rd line - num_ret
            res.num_ret = Integer.parseInt(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            line = reader.readLine(); // 4th line - num_rel
            res.num_rel = Integer.parseInt(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            line = reader.readLine(); // 5th line - num_rel_ret
            res.num_rel_ret = Integer.parseInt(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            line = reader.readLine(); // 6th line - map
            res.map = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            line = reader.readLine(); // 7th line - gm_map
            res.gm_map = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            line = reader.readLine(); // 8th line - Rprec
            res.rprec = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            line = reader.readLine(); // 9th line - bpref
            res.bpref = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            line = reader.readLine(); // 10th line - recip_rank
            res.recip_rank = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            res.iprec_at_recall = new double[11];
            line = reader.readLine(); // 11th line
            res.iprec_at_recall[0] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[1] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[2] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[3] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[4] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[5] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[6] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[7] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[8] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[9] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine();
            res.iprec_at_recall[10] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            
            res.iprec_at_k = new double[9];
            line = reader.readLine(); // P_5
            res.iprec_at_k[0] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_10
            res.iprec_at_k[1] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_15
            res.iprec_at_k[2] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_20
            res.iprec_at_k[3] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_30
            res.iprec_at_k[4] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_100
            res.iprec_at_k[5] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_200
            res.iprec_at_k[6] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_500
            res.iprec_at_k[7] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
            line = reader.readLine(); // P_1000
            res.iprec_at_k[8] = Double.parseDouble(line.substring(line.indexOf("\tall\t") + 5).trim());
       
            return res;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("The output error. Please check.");
		}
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//typical for AIRS 2013 topic set
//		Evaluator eval = new Evaluator("/Volumes/ZTZ-99/profession.workspace/airs2013/qrel/qrel-all", 
//				"/Volumes/ZTZ-99/index/trec2013_lucene_index");
//		File rfile = eval.search("1", "RAW", "hillary clinton resign");
//		System.out.println(rfile.getAbsolutePath());
//		eval.evalute(new File("/Volumes/ZTZ-99/profession.workspace/ndeval/tmp/MBD001-RAW-20144703723.result"));
//		NDEvalResult res = eval.evalute(rfile);
//		System.out.println("alpha-nDCG@20 - " + res.anDCG[2]);
		
		// Using Tweets2011 with TREC2011 / 2012 topics to identify the meaningful concepts
		Evaluator eval = new Evaluator("/Volumes/ZTZ-99/profession.workspace/trec2011/qrels_2011.txt", 
				"/Volumes/ZTZ-99/index/trec2011_lucene_index");
		File rfile = eval.search("1", "twitter-diversity-mc-1step", "BBC World Service staff cuts");
		TRECEvalResult res = eval.TRECEvalute(rfile);
		System.out.println("Mean Average Precision - " + res.map);
	}
}
