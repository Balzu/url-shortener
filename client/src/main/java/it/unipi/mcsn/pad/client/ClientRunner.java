package it.unipi.mcsn.pad.client;

import java.awt.DisplayMode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.zip.CRC32;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.json.JSONException;

import it.unipi.mcsn.pad.core.communication.client.ClientCommunicationManager;
import it.unipi.mcsn.pad.core.message.GetMessage;
//import it.unipi.mcsn.pad.core.message.ListMessage;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.PutMessage;
import it.unipi.mcsn.pad.core.message.RemoveMessage;

public class ClientRunner {
	
	
	
	
	public static void main (String args[]){
		
		
		try {			
			
			
			CommandLineManager clm = new CommandLineManager(args); 
			
			// If asked help, show help message and quit
			if (clm.needHelp()){
				clm.printHelp();
				System.exit(0);
			}
			
			// Use default or custom configuration file
			File configFile;
			if (clm.hasConfigFile()){
				String path = clm.getConfigurationPath();
				configFile = new File ( path + "/client.conf");
			}
			else{
				configFile = new File ("src/main/resources/client.conf");
			}
			System.out.println("Configuration file exists? " + configFile.exists()); // TODO throw exception if config file does not exist?
			ClientConfig cc = new ClientConfig(configFile);
			Client c = new RandomClient(cc); 
						
			Message msg = null;
			String url;
			
			//Interactive or non interactive session
			 if (clm.isInteractive()) {
				 //boolean isInteractive = true;
				 System.out.println("Started interactive session"); 
				 displayInteractiveMessage();
				 BufferedReader stdIn = new BufferedReader(
					new InputStreamReader(System.in));	
				 String input;
				 while (true){		
					msg = null; 
					System.out.println( "Enter operation" );				    
					input = stdIn.readLine();
								
					switch (input) {
				    case "put":
				    	System.out.println( "Enter url" );
						url = stdIn.readLine();
				    	msg = new PutMessage(url);	    		
				    	break;
				    case "get":
				    	System.out.println( "Enter url" );
						url = stdIn.readLine();
				    	msg = new GetMessage(url);
				    	break;				   
				    case "remove":
				    	System.out.println( "Enter url" );
						url = stdIn.readLine();
				    	msg = new RemoveMessage(url);
				    	break;
				    case "quit":
				    	System.out.println( "Quitting interactive session..." );
				    	System.exit(0);
				    default:
				    	System.err.println("Invalid operation! \n");
				    	displayInteractiveMessage();
				    }	
					if (msg != null){
						NodeMessage reply = (NodeMessage)c.sendRequest(msg);						 
						if (clm.hasOutputFile())				 
							writeOutputFile(clm, msg, reply);				 
						else				 
						    System.out.println(formatReply(msg, reply));
					}					 
				 }	 				  
			}
			 else
			 {				 
				 Option operation = clm.getOperation();
				 url = operation.getValue();
				 if (operation.getOpt() == clm.getPut())
					 msg = new PutMessage(url);
				 if (operation.getOpt() == clm.getGet())
					 msg = new GetMessage(url);
				 if (operation.getOpt() == clm.getRemove())
					 msg = new RemoveMessage(url);
				 else{
					 clm.printHelp();
					 System.exit(1);
				 }
				
				 NodeMessage reply = (NodeMessage)c.sendRequest(msg);					
				 
				 if (clm.hasOutputFile())				 
					 writeOutputFile(clm, msg, reply);				 
				 else				 
					System.out.println(formatReply(msg, reply)); 		 
			 } 		
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Use \"--help\" to show general usage information about client's command line");
		}
    	
	}
	
	private static String formatReply (Message msg, NodeMessage reply){
		return  "Response for the " + msg.getMessageType() + " request: \n"+
				 "Reply status: " + reply.getMessageStatus() + "\n" +
				 "Reply content: " + reply.toString();
	}
	
	private static void writeOutputFile(CommandLineManager clm, Message msg, NodeMessage reply) throws IOException{
		File outputFile = new File(clm.getOutputFileName());
		try ( Writer writer = new BufferedWriter(
				 new FileWriter(outputFile, true))) {
			 writer.write(formatReply(msg, reply));
	     }
	}
	
	private static void displayInteractiveMessage(){
		System.out.println("Usage: \n" +
				"- put                 to shorten and insert a url \n" +
				"- get                 to get the associated, long url \n"+
				"- remove              to remove a url \n" +
				"- quit                to quit the interactive session \n");
	}


}
