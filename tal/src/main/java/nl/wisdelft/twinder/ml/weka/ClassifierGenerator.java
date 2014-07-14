/**
 * 
 */
package nl.wisdelft.twinder.ml.weka;

import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * This class helps with generating a classifier that can be used for later application.
 * @author ktao
 *
 */
public class ClassifierGenerator {

	
	public static Logistic getClassifier(String url, String configuration) {
		try {
			DataSource source = new DataSource(url);
			Instances data = source.getDataSet();
			// setting class attribute if the data format does not provide this information
			// E.g., the XRFF format saves the class attribute information as well
			if (data.classIndex() == -1){
			  data.setClassIndex(data.numAttributes() - 1);
			}
			
			// train classifier
			CostSensitiveClassifier costSensitive = new CostSensitiveClassifier();
			costSensitive.setOptions(weka.core.Utils.splitOptions(configuration));
			 
			//remove bad features:
//			if(indexOfFeaturesToRemoveFromTheARFFFiles != null){
//				Remove rm = new Remove();
//				String[] options = new String[2];
//				options[0] = "-R"; 
//				options[1] = indexOfFeaturesToRemoveFromTheARFFFiles; //remove   
//				rm.setOptions(options);
//				rm.setInputFormat(data);
//				data = Filter.useFilter(data, rm);
//			}
			
			//learn model based on complete dataset to get the coefficients
			costSensitive.buildClassifier(data);
			Logistic logReg = (Logistic) costSensitive.getClassifier();
			return logReg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Null returned while trying to get the classifier!");
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
