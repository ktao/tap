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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.AlchemyAPI_NamedEntityParams;

/**
 * Annotate topics using the Alchemy API (via Genius library , i.e. dependency on https://svn.st.ewi.tudelft.nl/wis/persweb/persweb-genius/).
 * 
 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
 * @author last edited by: $Author: $
 * 
 * @version created on Aug 9, 2011
 * @version $Revision: $ $Date: $
 */
public class AlchemyAnnotator {
	
	/** Alchemy API Key */
	public static final String API_KEY = PropertyReader.getString("tal.apikey.alchemy");
	
	/** the Alchemy API instance */
	public static AlchemyAPI alchemyAPI = AlchemyAPI.GetInstanceFromString(API_KEY);
	
	/**
	 * POJO for entities extracted from text.
	 * 
	 * @author Fabian Abel, <a href="mailto:f.abel@tudelft.nl">f.abel@tudelft.nl</a>
	 * @version created on Aug 9, 2011
	 */
	public class ExtractedEntity {
		/** the name/label of the entity */
		public String name = null;
		
		/** the uri of the entity */
		public String uri = null;
		
		/** the type of the entity */
		public String type = null;
		
		/** the certainty score with which the entity was extracted */
		public Double score = null;
		
		/** the text based on which the entity was extracted */
		public String text = null;
	}
	
	
	/**
	 * Named entity extraction based on Alchemy.
	 * @param text the text from which to extract named entities
	 * @return the extracted entities
	 */
	public static List<ExtractedEntity> extractEntities(String text){
		List<ExtractedEntity> extractedConcepts = new ArrayList<ExtractedEntity>();
		AlchemyAPI_NamedEntityParams conf = new AlchemyAPI_NamedEntityParams();
		conf.setLinkedData(true);
		conf.setDisambiguate(true);
		
		try {
			Document doc = alchemyAPI.TextGetRankedNamedEntities(text, conf);
			
			//get entities from response:
			 XPath xpath = XPathFactory.newInstance().newXPath();
			 XPathExpression queryEntity = xpath.compile("//entities/entity");
			 XPathExpression queryProperties = null;
			 String queryResult = null;
			 
			 NodeList result = (NodeList) queryEntity.evaluate(doc, XPathConstants.NODESET);
			 
			 for (int i = 0; i < result.getLength(); i++) {
				 ExtractedEntity entity = new AlchemyAnnotator().new ExtractedEntity();
				 
				 queryProperties = xpath.compile("type/text()");
				 queryResult = (String)queryProperties.evaluate(result.item(i), XPathConstants.STRING);
				 if(queryResult != null && !"".equals(queryResult)){
					 entity.type = queryResult;
				 }
				 
				 queryProperties = xpath.compile("text/text()");
				 queryResult = (String)queryProperties.evaluate(result.item(i), XPathConstants.STRING);
				 if(queryResult != null && !"".equals(queryResult)){
					 entity.text = queryResult;
				 }
				 
				 queryProperties = xpath.compile("relevance/text()");
				 queryResult = (String)queryProperties.evaluate(result.item(i), XPathConstants.STRING);
				 if(queryResult != null && !"".equals(queryResult)){
					 entity.score = Double.valueOf(queryResult);
				 }
				 
				 queryProperties = xpath.compile("disambiguated/name/text()");
				 queryResult = (String)queryProperties.evaluate(result.item(i), XPathConstants.STRING);
				 if(queryResult != null && !"".equals(queryResult)){
					 entity.name = queryResult;
				 }
				 
				 queryProperties = xpath.compile("disambiguated/dbpedia/text()");
				 queryResult = (String)queryProperties.evaluate(result.item(i), XPathConstants.STRING);;
				 if(queryResult != null && !"".equals(queryResult)){
					 entity.uri = queryResult;
				 }
				 
				 extractedConcepts.add(entity);
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return extractedConcepts;
	}
	
	/**
	 * Just for debugging
	 * @param doc document to output
	 * @return string representaion of the document
	 */
    @SuppressWarnings("unused")
	private static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
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
