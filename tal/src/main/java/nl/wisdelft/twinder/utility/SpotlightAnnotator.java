/**
 * 
 */
package nl.wisdelft.twinder.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
//import org.jdom2.Namespace;
//import org.jdom2.input.SAXBuilder;


/**
 * This class is for crawl entities in a text via <a href="http://dbpedia.org/spotlight">DBpedia Spotlight</a>.
 * The function is implemented in a method called annotate. Given the text, the confidence and the support, the 
 * method will return a HashMap, the key is entity in the form of the original text, while the value is an instance
 * of the entity class SpotlightEntity. It's composite of two URIs, including the resource URI, type URI.
 * 
 * NB: For some entities the type URI maybe null.
 * 
 * 4 formats supported.
 * 
 * The method annotate can be fed with another parameter output, the possible value can be found as the constant integer
 * in the class. If the parameter is wrongly specified, the default output will be xhtml+xml.
 * 
 * @author Ke Tao <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: $Author: ktao $
 * 
 * @version created on Feb 15, 2011
 */
public class SpotlightAnnotator {

//	private static final Namespace NAMESPACE = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
	
	public static final int OUTPUT_HTML = 1;
	public static final int OUTPUT_XML = 2;
	public static final int OUTPUT_XHTML = 3;
	public static final int OUTPUT_JSON = 4;
	
	
	/**
	 * 
	 * @param text
	 * @param confidence
	 * @param support
	 * @return
	 */
	public static HashMap<String, SpotlightEntity> annotate(String text, double confidence, int support) {
		HashMap<String, SpotlightEntity> result = new HashMap<String, SpotlightEntity>(); 
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("text", text));
		qparams.add(new BasicNameValuePair("confidence", Double.toString(confidence)));
		qparams.add(new BasicNameValuePair("support", Integer.toString(support)));
		
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "spotlight.dbpedia.org", -1, "/rest/annotate",
//			uri = URIUtils.createURI("http", "apsthree.st.ewi.tudelft.nl", 2222, "/rest/annotate",
				    URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		HttpGet httpget = new HttpGet(uri);
		
		HttpClient httpclient = new DefaultHttpClient();
		httpget.setHeader("Accept", "application/json");
		httpget.setHeader("Connection", "close");
		httpclient.getParams().setBooleanParameter("http.protocol.expect-continue", false); 
		
		HttpResponse response = null;
		
		try {
			response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HttpEntity entity = response.getEntity();
		InputStream instream = null;
		if (entity != null) {
		    try {
//		    	SAXBuilder builder = new SAXBuilder();
				instream = entity.getContent();
				
				String jsonString = "";
				for(int i = instream.read(); i != -1; i = instream.read())
		        {
					jsonString += (char) i;
		        }
				
				JSONObject jsonObj = null;
				
				jsonObj = new JSONObject(jsonString);
				
				JSONArray resources = jsonObj.getJSONArray("Resources");
				
				for(int i = resources.length(); i > 0; i--) {
					JSONObject resource = resources.getJSONObject(i - 1);
					SpotlightEntity sle = new SpotlightEntity();
					
					sle.setResource(new URI(resource.getString("@URI")));
					sle.setScore(Double.parseDouble(resource.getString("@similarityScore")));
					sle.setType(resource.getString("@types"));
					sle.name = DBpediaURIUtility.convertDBpediaURI2Name(resource.getString("@URI"));
					sle.text = resource.getString("@surfaceForm");
					result.put(sle.name, sle);
				}
		    } catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (JSONException e) {
//				e.printStackTrace();
				System.out.println("Failed text: " + text);
			} finally {
		        try {
					instream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		}
				
		return result; 
	}
		
	public static class SpotlightEntity {
		public String text;
		public URI resource;
//		private List<URI> type;
		public String type;
		public double score;
		public String name;

		public URI getResource() {
			return resource;
		}

		protected void setResource(URI resource) {
			this.resource = resource;
		}
		
		public String getType() {
			return type;
		}
		
		protected void setType(String type) {
			if(type == null) {
				this.type = "";
			} else {
				this.type = type;
			}
		}

//		protected List<URI> getType() {
//			return type;
//		}
//
//		protected void setType(List<URI> type) {
//			this.type = type;
//		}
		
		public double getScore() {
			return score;
		}
		
		protected void setScore(double score) {
			this.score = score;
		}
	}
	
	/**
	 * The multiple output format is supported in this method.
	 * @param text The text to be spotlighted.
	 * @param confidence
	 * @param support
	 * @param output The expected output format, html(1), xhtml+xml(2), xml(3), json(4).
	 * @return
	 */
	public static String annotate(String text, double confidence, int support, int output) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("text", text));
		qparams.add(new BasicNameValuePair("confidence", Double.toString(confidence)));
		qparams.add(new BasicNameValuePair("support", Integer.toString(support)));
		
		URI uri = null;
		try {
			uri = URIUtils.createURI("http", "spotlight.dbpedia.org", -1, "/rest/annotate",
//			uri = URIUtils.createURI("http", "apsthree.st.ewi.tudelft.nl", 2222, "/rest/annotate", 
				    URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		HttpGet httpget = new HttpGet(uri);
		
		HttpClient httpclient = new DefaultHttpClient();
		switch(output) {
			case OUTPUT_HTML:
				httpget.setHeader("Accept", "text/html");
				break;
			case OUTPUT_XHTML:
				httpget.setHeader("Accept", "application/xhtml+xml");
				break;
			case OUTPUT_XML:
				httpget.setHeader("Accept", "text/xml");
				break;
			case OUTPUT_JSON:
				httpget.setHeader("Accept", "application/json");
				break;
			default:
				httpget.setHeader("Accept", "application/xhtml+xml");
				break;
		}

		httpget.setHeader("Connection", "close");
		httpclient.getParams().setBooleanParameter("http.protocol.expect-continue", false);

		ResponseHandler<byte[]> handler = new ResponseHandler<byte[]>() {
		    public byte[] handleResponse(
		            HttpResponse response) throws ClientProtocolException, IOException {
		        HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            return EntityUtils.toByteArray(entity);
		        } else {
		            return null;
		        }
		    }
		};
		
		byte[] response = null;

		try {
			response = httpclient.execute(httpget, handler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String responseString = new String(response);
		
		return responseString;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		HashMap<String, SpotlightEntity> result = annotate("President Obama called Wednesday on Congress to extend a tax break for students included" +
//				" in last year's economic stimulus package, arguing that the policy provides more " +
//				"generous assistance.", 0.3, 30);
//		Set<String> keySet = result.keySet();
//		for(String key : keySet ) {
//			System.out.println("URI : " + result.get(key).getResource());
//			System.out.println("Score : " + result.get(key).getScore());
//			System.out.println("Type : " + result.get(key).getType());
//		}
		
//		ResultSet rs = DBUtility.executeQuerySingleConnection("SELECT creationTime FROM tweets WHERE creationTime < '" + new Timestamp(Long.parseLong("1295741800000")) + "' LIMIT 10");
//		
//		try {
//			if(rs.next()) {
//				System.out.println(rs.getBoolean(1));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		
		String json = annotate("President Obama called Wednesday on Congress to extend a tax break for students included" +
				" in last year's economic stimulus package, arguing that the policy provides more " +
				"generous assistance.", 0.2, 20, SpotlightAnnotator.OUTPUT_JSON);
		System.out.println(json);
		
		try {
			JSONObject jsonObj = new JSONObject(json);
			
			JSONArray resources = jsonObj.getJSONArray("Resources");
			
			for(int i = resources.length(); i > 0; i--) {
				JSONObject resource = resources.getJSONObject(i - 1);
				System.out.println("String: " + resource.getString("@surfaceForm"));
				System.out.println("URI: " + resource.getString("@URI"));
				System.out.println("Score:" + resource.getString("@similarityScore"));
				System.out.println("Name:" + DBpediaURIUtility.convertDBpediaURI2Name(resource.getString("@URI")));
				System.out.println();
				System.out.println();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}