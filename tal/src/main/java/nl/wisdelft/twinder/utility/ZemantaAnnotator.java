/*********************************************************
*  Copyright (c) 2010 by Web Information Systems (WIS) Group.
*  Fabian Abel, http://fabianabel.de/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.utility;

import java.util.ArrayList;
import java.util.List;

import nl.wisdelft.twinder.utility.AlchemyAnnotator.ExtractedEntity;

import com.zemanta.api.Zemanta;
import com.zemanta.api.ZemantaResult;
import com.zemanta.api.suggest.Markup.Link;
import com.zemanta.api.suggest.Markup.Target;

/**
 * Extract Wikipedia Links with Zemanta and translate them into DBpedia URIs.
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @author last edited by: $Author: $
 * 
 * @version created on Aug 9, 2011
 * @version $Revision: $ $Date: $
 */
public class ZemantaAnnotator {
	public static final String ZEMAMTA_API_KEY = PropertyReader.getString("tal.apikey.zemanta");
	
	/**
	 * Named entity extraction based on Zemanta.
	 * @param text the text from which to extract named entities
	 * @return the extracted entities
	 */
	public static List<ExtractedEntity> extractEntities(String text){
		List<ExtractedEntity> extractedConcepts = new ArrayList<ExtractedEntity>();
		Zemanta zem = new Zemanta(ZEMAMTA_API_KEY);	
		ZemantaResult zemResult = zem.suggest(text);
		String cid = zemResult.rid;
		for(Link link : zemResult.markup.links){
			String term = link.anchor;
			for(Target target : link.targets){
				if(target.url != null && target.url.startsWith("http://en.wikipedia.org/wiki/")){
					ExtractedEntity entity = new AlchemyAnnotator().new ExtractedEntity();
					entity.name = target.title;
					entity.type = target.type.name();
					entity.uri = target.url.replace("http://en.wikipedia.org/wiki/", "http://dbpedia.org/resource/");
					entity.score = new Float(link.confidence).doubleValue();
					entity.text = link.anchor;
					extractedConcepts.add(entity);
				}
			}
		}
		
		return extractedConcepts;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//testing:
		List<ExtractedEntity> extractedConcepts = extractEntities("Frensh Open winner Roger Federer won tennis cup. Is Federer driving a jaguar car like Obama or Kennedy?");
		for (ExtractedEntity concept : extractedConcepts) {
			System.out.println("Concept: " + concept.name + " [type: " + concept.type + ", uri: " + concept.uri + ", score: " + concept.score + ", annotated text: " + concept.text + "]" );
		}
	}
}
