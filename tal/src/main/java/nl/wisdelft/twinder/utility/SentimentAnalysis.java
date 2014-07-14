/**
 * 
 */
package nl.wisdelft.twinder.utility;

/**
 * The interface for different sentiment analysis services, either local library
 * or external services.
 *  
 * @author ktao
 */
public interface SentimentAnalysis {

	/**
	 * Given a textual snippet, return the sentiment polarity of it.
	 * @param content The given textual snippet
	 * @return sentiment polarity (0 = negative, 2 = neutral, 4 = positive)
	 */
	public int classifySentiment(String content);
}
