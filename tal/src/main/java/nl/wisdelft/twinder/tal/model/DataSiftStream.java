/*********************************************************
*  Copyright (c) 2013 by Web Information Systems (WIS) Group.
*  Ke Tao, http://ktao.nl/
*
*  Some rights reserved.
*
*  Contact: http://www.wis.ewi.tudelft.nl/
*
**********************************************************/
package nl.wisdelft.twinder.tal.model;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.datasift.client.DataSiftClient;
import com.datasift.client.DataSiftConfig;
import com.datasift.client.core.Stream;
import com.datasift.client.stream.DataSiftMessage;
import com.datasift.client.stream.DeletedInteraction;
import com.datasift.client.stream.ErrorListener;
import com.datasift.client.stream.Interaction;
import com.datasift.client.stream.StreamEventListener;
import com.datasift.client.stream.StreamSubscription;

import nl.wisdelft.twinder.tal.source.datasift.DataSiftWrapper;

/**
 * Example of processing data stream received from DataSift.
 * 
 * Compatiable with DataSift Client API 3.0.0+
 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
 * @author last edited by: ktao
 * 
 * @version created on Apr 22, 2014
 */
public class DataSiftStream {

	public static int counter = 10000;
	public static BufferedWriter bwriter;
	
	/**
	 * Test with example username and API key
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String... args) throws InterruptedException {
		DataSiftConfig config = new DataSiftConfig("ktao.gmail", "e34f541ee25ecaf7b83093282b68bec2");
		final DataSiftClient datasift = new DataSiftClient(config);
        
        Stream stream = Stream.fromString("31a194335a54e63d099f2086bfbba132");
        
        try {
			bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/ktao/Desktop/ds_stream_10000.json")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

        //handle exceptions that can't necessarily be linked to a specific stream
        datasift.liveStream().onError(new ErrorHandler());

        //handle delete message
        datasift.liveStream().onStreamEvent(new StreamEventListener() {
			
			@Override
			public void onDelete(DeletedInteraction di) {
				// TODO handle the delete action instead of just printing the stack trace
				System.out.println("Interaction deleted: " + di.tweetId());
			}
		});
        
        datasift.liveStream().subscribe(new Subscription(stream));
        
        //process interactions for another stream ignore processing another stream
        //reserved for combine two resources.
        //datasift.liveStream().subscribe(new Subscription(Stream.fromString("another-stream-hash")));

        //at some point later if you want unsubscribe
        //datasift.liveStream().unsubscribe(stream);
    }

	/**
	 * The subclass for handling the processing of each Twitter message that is received by Twinder.
	 * 
	 * @author Ke Tao, <a href="mailto:k.tao@tudelft.nl">k.tao@tudelft.nl</a>
	 * @author last edited by: ktao
	 * 
	 * @version created on Apr 22, 2014
	 */
    public static class Subscription extends StreamSubscription {
        public Subscription(Stream stream) {
            super(stream);
        }

        public void onDataSiftLogMessage(DataSiftMessage di) {
            //di.isWarning() is also available
            System.out.println((di.isError() ? "Error" : di.isInfo() ? "Info" : "Warning") + ":\n" + di);
        }

        /**
         * Customize the processing of the messages.
         */
        public void onMessage(Interaction i) {
            //System.out.println(i.toString());
            
            try {
				bwriter.append(i.toString() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            if (counter % 50 == 0)
            	System.out.println(counter + " tweets crawled.");
            
            if (counter-- < 0)
            	System.exit(0);
            
            // enrichment
            
            // indexing
            // consider add it to Lucene Platform
        }
    }

    public static class DeleteHandler extends StreamEventListener {
        public void onDelete(DeletedInteraction di) {
            //go off and delete the interaction if you have it stored. This is a strict requirement!
            System.out.println("DELETED:\n " + di);
        }
    }

    public static class ErrorHandler extends ErrorListener {
        public void exceptionCaught(Throwable t) {
            t.printStackTrace();
            //do something useful...
        }
    }
    
    /**
     * The code for testing purposes....
     * 
     * 
     * 
     * 
     * 
     */
    public void test() {
    	// b4422493bb651139d21aceec08d36d12 stream - geo radius - tweets in New York City
//      Stream stream = Stream.fromString("b4422493bb651139d21aceec08d36d12");
      
		// c1a3f94f5023666193b48139d555ca24 stream - mention Delta, KLM, AirFrance, Alitalia
//      Stream stream = Stream.fromString("c1a3f94f5023666193b48139d555ca24");
    }
}
