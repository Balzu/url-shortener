package it.unipi.mcsn.pad.core.utils;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.PutMessage;
import it.unipi.mcsn.pad.core.message.ReplyMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;

public class MessageHandler {
	//TODO: no separation between handling of client and node message?
	public static Message handleMessage(Message msg, StorageService storageService){
		
		
		switch(msg.getMessageType())
		{
		  case GET:
			  return processGetMessage(msg);			  
		  case PUT:
			  return processPutMessage(msg);
			 
		  case LIST:
			  return processListMessage(msg);
			  
		  case REMOVE:
			  return processRemoveMessage(msg);
			  
		  default:
			  return new ReplyMessage(null,null, MessageStatus.ERROR);
		}	
		
	}
	
	private static Message processGetMessage(Message msg){
		return null;
	}
	
    private static Message processPutMessage(Message msg){
    	PutMessage pmsg = (PutMessage)msg;
    	System.out.println("Processing put message...");
    	Message reply = new ReplyMessage(pmsg.getLongUrl(), "short_url", MessageStatus.SUCCESS);
		return reply;
	}
    
    private static Message processListMessage(Message msg){
    	return null;
	}
    
    private static Message processRemoveMessage(Message msg){
    	return null;
	}

}
