/**
 * 
 */
package nl.wisdelft.twinder.io;

import com.mongodb.BasicDBObject;

/**
 * @author ktao
 *
 */
public interface DBObjectCovertable {
	public BasicDBObject toBasicDBObject();
}
