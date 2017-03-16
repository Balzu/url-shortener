package it.unipi.mcsn.pad.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;

import it.unipi.mcsn.pad.core.message.GetMessage;
import it.unipi.mcsn.pad.core.message.ListMessage;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.PutMessage;
import it.unipi.mcsn.pad.core.message.RemoveMessage;
import it.unipi.mcsn.pad.core.message.ReplyMessage;

public class ClientRunner {
	
	public static void main (String args[]){
		
		
		try {
			
			//TODO: Manage configuration also through command line arguments
			File configFile = new File ("src/main/resources/client.conf");
			System.out.println("File exists? " + configFile.exists());			
			ClientConfig cc = new ClientConfig(configFile);
			Client c = new RandomClient(cc);
			System.out.println( "Enter operation" );			
	    	BufferedReader stdIn = new BufferedReader(
	    			new InputStreamReader(System.in));	    
	    	String input;
			input = stdIn.readLine();
			System.out.println( "Enter url" );
			String url = stdIn.readLine();
			Message msg = null;
			switch (input) {
	    	case "put":
	    		msg = new PutMessage(url);	    		
	    		break;
	    	case "get":
	    		msg = new GetMessage(url);
	    		break;
	    	case "list":
	    		msg = new ListMessage();
	    		break;
	    	case "remove":
	    		msg = new RemoveMessage();
	    		break;
	    	default:
	    		System.err.println("invalid operation");
	    		System.exit(1);
	    	}
			
			ReplyMessage reply = (ReplyMessage)c.sendRequest(msg);			
			System.out.println("Request response:");
			System.out.println(reply.getMessageStatus());
			System.out.println(reply.toString());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	


}
