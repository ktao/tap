package nl.wisdelft.twinder;

import nl.wisdelft.twinder.lucene.Indexer;
import nl.wisdelft.twinder.utility.PropertyReader;

/**
 * Hello world!
 *
 */
public class App 
{
	private Indexer indexer;
	// data source
	
	public App() {
		// default configuration file
		indexer = new Indexer(PropertyReader.getString("tal.index.path"));
		// source
		// create a source streaming task with ...
	}
	
	public App(String configFile) {
		
	}
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
