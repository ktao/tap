/**
 * 
 */
package nl.wisdelft.twinder.utility;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

/**
 * @author ktao
 *
 */
public class HTTPUtility {

	public static String invokeRESTful(String url, String bulk) {
		StringBuilder sbuilder = new StringBuilder();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);

//            File file = new File(filename);

            ByteArrayInputStream bais = new ByteArrayInputStream(bulk.getBytes());
            InputStreamEntity reqEntity = new InputStreamEntity(
            		bais, -1, ContentType.APPLICATION_OCTET_STREAM);
            reqEntity.setChunked(true);
            // It may be more appropriate to use FileEntity class in this particular
            // instance but we are using a more generic InputStreamEntity to demonstrate
            // the capability to stream out data from any arbitrary source
            //
            // FileEntity entity = new FileEntity(file, "binary/octet-stream");

            httppost.setEntity(reqEntity);

//            System.out.println("Executing request: " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
//                System.out.println("----------------------------------------");
//                System.out.println(response.getStatusLine());
                InputStreamReader in = new InputStreamReader(response.getEntity().getContent(), "UTF-8");
                char[] buffer = new char[4096];
                int len = -1;
				while ((len = in.read(buffer)) > 0) {
                	sbuilder.append(buffer, 0, len);
                }
            } finally {
                response.close();
            }
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return sbuilder.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// test
//		System.out.println(invokeRESTful("http://www.sentiment140.com/api/bulkClassifyJson?appid=k.tao.tudelft@gmail.com", 
//				"/Volumes/ZTZ-99/profession.workspace/twinder/sentiment/bulk-553"));
		
		String bulk = "{\"data\": [{\"text\": \"I love Titanic.\", \"id\": 1234}, {\"text\": \"I hate Titanic.\", \"id\": 4567}]}";
		System.out.println(invokeRESTful("http://www.sentiment140.com/api/bulkClassifyJson?appid=k.tao.tudelft@gmail.com", bulk));
	}

}
