/**
 * 
 */
package nl.wisdelft.twinder.utility;

/**
 * @author ktao
 *
 */
public class DBpediaURIUtility {

	public static String convertDBpediaURI2Name(String uri) {
		return decodeDBpediaURI(uri).replace("http://dbpedia.org/resource/", "");
	}
	
	public static String decodeDBpediaURI(String uri) {
		String result = uri.replace('_', ' ');
		result = result.replace("%21", "!");
		result = result.replace("%22", "\"");
		result = result.replace("%23", "#");
		result = result.replace("%24", "$");
		result = result.replace("%27", "\'");
		result = result.replace("%28", "(");
		result = result.replace("%29", ")");
		result = result.replace("%2B", "+");
		result = result.replace("%3B", ";");
		result = result.replace("%3C", "<");
		result = result.replace("%3D", "=");
		result = result.replace("%3E", ">");
		result = result.replace("%3F", "?");
		result = result.replace("%40", "@");
		result = result.replace("%5B", "[");
		result = result.replace("%5C", "\\");
		result = result.replace("%5D", "]");
		result = result.replace("%5E", "^");
		result = result.replace("%60", "`");
		result = result.replace("%7B", "{");
		result = result.replace("%7C", "|");
		result = result.replace("%7D", "}");
		result = result.replace("%7E", "~");
		return result.replace("%25", "%");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
