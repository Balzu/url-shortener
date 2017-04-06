package it.unipi.mcsn.pad.core.utils;

import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.PutMessage;
import it.unipi.mcsn.pad.core.message.ReplyMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;
import voldemort.versioning.Version;
import voldemort.versioning.Versioned;

public class MessageHandler {
	
	//TODO: no separation between handling of client and node message?
	/*
	public static NodeMessage handleMessage(ClientMessage msg, StorageService storageService)
	{		
		switch(msg.getMessageType())
		{
		  case GET:
			  return processGetMessage(msg.getUrl(), storageService);	
			  
		  case PUT:
			  return processPutMessage(msg, storageService); 		
			  
		  case REMOVE:
			  return processRemoveMessage(msg, storageService);
		//TODO ReplyMessage can be eliminated, and instead use a NodeMessage ?	  
		  default:
			  return new VersionedMessage(null,null, null, null, MessageStatus.ERROR);
		}	
		
	}
	*/
	
	public static NodeMessage handleMessage(NodeMessage nmsg, StorageService storageService){
		//TODO merge received vector clock with node's vector clock?
		switch(nmsg.getMessageType())
		{
		  case GET:
			  return processGetMessage(nmsg, storageService);	
			  
		  case PUT:
			  return processPutMessage(nmsg, storageService); 		
			  
		  case REMOVE:
			  return processRemoveMessage(nmsg, storageService);
		//TODO ReplyMessage can be eliminated, and instead use a NodeMessage ?	  
		  default:
			  return new VersionedMessage(null,null, null, null, MessageStatus.ERROR);
		}	
	}
	
	private static NodeMessage processGetMessage(
			NodeMessage nmsg, StorageService storageService)
	{		
		//TODO handle the case of error? Maybe read() can throw an exception and handle it here
		// by creating a msg with ERROR status
		String surl = nmsg.getShortUrl();
		Versioned<String> vlurl = storageService.getStorageManager().read(surl);	
		if (vlurl == null)
			return (new VersionedMessage(null, null, null, MessageType.REPLY, MessageStatus.ERROR));
		String lurl = vlurl.getValue();
		Version vc = vlurl.getVersion();
		return (new VersionedMessage(lurl, surl, vc, MessageType.REPLY, MessageStatus.SUCCESS));
	}
	
    private static NodeMessage processPutMessage(
    	NodeMessage nmsg, StorageService storageService){
    	boolean stored = storageService.getStorageManager().store(nmsg);
    	if (stored){    	
    		return (new VersionedMessage(
				nmsg.getLongUrl(), nmsg.getShortUrl(), nmsg.getVectorClock(),
				MessageType.REPLY, MessageStatus.SUCCESS));
    	}
    	return (new VersionedMessage(
				null, null, null,
				MessageType.REPLY, MessageStatus.ERROR));
	}    
 
    
    private static NodeMessage processRemoveMessage(
    		NodeMessage nmsg, StorageService storageService){
    	
    	Versioned<String> vlurl = storageService.getStorageManager().remove(nmsg);
    	if (vlurl == null){    	
    		return (new VersionedMessage(
    				null, null, null,
    				MessageType.REPLY, MessageStatus.ERROR));
    	}    	
    	String surl = nmsg.getShortUrl();
    	String lurl = vlurl.getValue();
		Version vc = vlurl.getVersion();
    	return (new VersionedMessage(lurl, surl, vc,
				MessageType.REPLY, MessageStatus.SUCCESS));
	}

}
