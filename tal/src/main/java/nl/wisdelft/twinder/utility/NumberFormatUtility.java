/**
 * 
 */
package nl.wisdelft.twinder.utility;

/**
 * @author ktao
 *
 */
public class NumberFormatUtility {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static double roundTo1(double value) {
		if (value > 1.0)
			return 1.0;
		else if (value < 0.0)
			return 0.0;
		else
			return value;
	}
}
