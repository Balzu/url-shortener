package it.unipi.mcsn.pad.core.utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import it.unipi.mcsn.pad.core.communication.node.ReplicaManager;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.SizedBackupMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;
import it.unipi.mcsn.pad.core.storage.StorageManager;
import it.unipi.mcsn.pad.core.storage.StorageService;
import voldemort.versioning.Version;
import voldemort.versioning.Versioned;

public class MessageHandler {	
	
	
	public static Message handleMessage(Message msg, StorageService storageService, ReplicaManager rm){		
		if (msg instanceof NodeMessage){
			NodeMessage nmsg = (NodeMessage) msg;
			switch(nmsg.getMessageType())
			{
			  case GET:
				  return processGetMessage(nmsg, storageService);	
				  
			  case PUT:
				  return processPutMessage(nmsg, storageService); 		
				  
			  case REMOVE:
				  return processRemoveMessage(nmsg, storageService, rm);
			  default:
				  return new VersionedMessage(null,null, null, null, MessageStatus.ERROR);
			}
		}
		else if(msg instanceof UpdateMessage ){
			UpdateMessage umsg = (UpdateMessage) msg;
			return processUpdateMessage(umsg, storageService, rm);
		}
		return null; //Should never end up returning null
	}
	
	private static NodeMessage processGetMessage(
			NodeMessage nmsg, StorageService storageService)
	{			
		String surl = nmsg.getShortUrl();			
		Versioned<String> vlurl = storageService.getStorageManager().read(nmsg);
		if (vlurl == null)
			return (new VersionedMessage(null, null, null, MessageType.REPLY, MessageStatus.ERROR));
		String lurl = vlurl.getValue();
		Version vers = vlurl.getVersion();
		return (new VersionedMessage(lurl, surl, vers, MessageType.REPLY, MessageStatus.SUCCESS));
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
    		NodeMessage nmsg, StorageService storageService, ReplicaManager rm)
    {    	
    	Versioned<String> vlurl = storageService.getStorageManager().remove(nmsg);
    	if (vlurl == null){    	
    		return (new VersionedMessage(
    				null, null, null,
    				MessageType.REPLY, MessageStatus.ERROR));
    	}    	
    	String surl = nmsg.getShortUrl();
    	String lurl = vlurl.getValue();
		Version vers = vlurl.getVersion();
		if (rm.isSetRemoved())  
			rm.getRemoved().add(surl);
    	return (new VersionedMessage(lurl, surl, vers,
				MessageType.REPLY, MessageStatus.SUCCESS));
	}
    
    private static UpdateMessage processUpdateMessage(
    		UpdateMessage umsg, StorageService storageService, ReplicaManager rm)
    {    	

    	Logger logger = Logger.getLogger("myLogger");
        // This can only happen if this node has to manage some of the urls of the backup node
    	if (umsg.getMessageType() == MessageType.RECOVERED){  
    		logger.debug("Node " + rm.getNodeId() + " manages some urls previously "
    				+ " assigned to node " + umsg.getSenderId());
    		return processUpdatesFromBackupNode(umsg,storageService,rm);
    	}
    		
    	
    	// If the node that sends me the backup leaves, prepare to handle the urls previously assigned to it and reset backup ids
    	if (umsg.getMessageType() == MessageType.LEAVE){
    		logger.info("Node " + rm.getNodeId() + " is notified of leaving of node " + umsg.getSenderId());
    		storageService.getStorageManager().mergeDB();  
   	        rm.setBackupId(-1);
   	        rm.setBackupIdTemp(-1);
    		return new SizedBackupMessage(0, rm.getNodeId(), false, MessageStatus.SUCCESS, MessageType.REPLY);
    	}    	     
    	
    	if  (umsg.getMessageType() == MessageType.NEW)
    	{
    		handleNewNode(umsg, storageService, rm);
    		return new SizedBackupMessage(0, rm.getNodeId(), false, MessageStatus.SUCCESS, MessageType.REPLY);
    	}			
    	
    	// If the replica manager never received a backup before, we store the id of the sending node in the
    	// replica manager to mean that from now on the replica manager expects update messages from that node
    	if (rm.getBackupId() == -1)
    	{    	
    			logger.debug("Node " + rm.getNodeId() + " sets its backup sender node to " + umsg.getSenderId());
    			rm.setBackupId(umsg.getSenderId()); 		
    	}
    		
    	// If the id of the sender is different from the id stored in the replica manager it means that 
    	// a node crashed. The replica manager must prepare 
    	// to receive the following messages from a different node: so it updates its backupIdTemp 
    	// to account for the new sender, and merges the backup DB into the primary DB (indeed from
    	// now on this node should answer also the queries concerning the crashed node)
    	else 
    		// First time I receive update from a node different from default sender
    		// I also check that considering the backup node down is not an error of the gossip protocol
    		if (umsg.getSenderId() != rm.getBackupId() && rm.getBackupIdTemp() == -1 
    		        && umsg.getMessageType()!=MessageType.NEW && rm.isNodeAlive(rm.getBackupId()) == false)    		  			
    		    handleCrashedNode(umsg, storageService, rm);   	    	    
    	    
    		else if (umsg.getSenderId() == rm.getBackupId()){  // The sender of the backup is who I expect
    			if(rm.getBackupIdTemp() != -1){ // true IFF sender recovered from crashing
    				handleRecoveredNode(umsg, storageService, rm); 
    				//End here: not going to store in the backup DB the outdated urls from the crashed node						
    				return new SizedBackupMessage(0, rm.getNodeId(), false, MessageStatus.SUCCESS, MessageType.REPLY);
    			}
    		}    		
    	
    	// The backup database is emptied every time a sequence of updates is started in order to keep it 
    	// updated.
    	if (umsg.isFirst())
    		storageService.getStorageManager().emptyBackup();    	
    	
    	boolean stored = storageService.getStorageManager().storeBackup(umsg.getitems());
    	if (!stored)
    		return new SizedBackupMessage(0, rm.getNodeId(), false, MessageStatus.ERROR, MessageType.REPLY);
    	return new SizedBackupMessage(0, rm.getNodeId(), false, MessageStatus.SUCCESS, MessageType.REPLY);
    }
    
 
    
    private static UpdateMessage processUpdatesFromBackupNode(
    		UpdateMessage umsg, StorageService storageService, ReplicaManager rm)
    {    	
    	StorageManager sman = storageService.getStorageManager();
    	for (Entry<String,Versioned<String>> e: umsg.getitems().entrySet()){
    		String surl = e.getKey();
    		if (!sman.contains(surl))
    			sman.store(surl, e.getValue());
    		else{
    			if (e.getValue()==null){
    				sman.remove(surl);
    		    	System.out.println("Rimosso url " + surl);}
    			else
    				sman.storeWithConflictResolution(surl, e.getValue());
    		}    			
    	}    	
    	//TODO: handle the case of failure with an ERROR message ?
    	return new SizedBackupMessage(0, rm.getNodeId(), false, MessageStatus.SUCCESS, MessageType.REPLY);
    }
    
    
    private static void handleNewNode(UpdateMessage umsg, StorageService storageService, ReplicaManager rm)
    {    	
		try {
			Logger logger = Logger.getLogger("myLogger");
	    	logger.debug("Node " + rm.getNodeId() + " receives backup from the NEW node " + umsg.getSenderId());	    	
	    	rm.setBackupId(umsg.getSenderId());
	    	rm.addMemberToGossipListIfDead(rm.getBackupId()); 
			storageService.getStorageManager().emptyBackup(); 			
			rm.partitionUrlsBetweenDB();			
			Map<String,Versioned<String>> dump = storageService.getStorageManager().getBackupDump(); 
			List<UpdateMessage> updates = new ArrayList<>();
			rm.createUpdatesToRecover(updates, dump); 
			rm.sendUpdates(rm.getBackupId(), updates);
		} catch (UnknownHostException | NullPointerException | InterruptedException | ExecutionException e) {					
			e.printStackTrace();
		}
    }
    
    
    private static void handleCrashedNode(UpdateMessage umsg, StorageService storageService, ReplicaManager rm){
    	Logger logger = Logger.getLogger("myLogger");
    	logger.info("Node " + rm.getNodeId() + " expects backup from " + 
		        + rm.getBackupId() + " but receives it from " + umsg.getSenderId());	
		logger.debug("Node Id =  " + rm.getNodeId() + ", backupId = " + 
    		        + rm.getBackupId() + ", backupIdTemp = " + rm.getBackupIdTemp() + ", Sender id = "+ umsg.getSenderId()
    		        + "Message type " + umsg.getMessageType());
		rm.setBackupIdTemp(umsg.getSenderId());
		rm.setRemoved();
		storageService.getStorageManager().mergeDB();    		
    }
    
    private static void handleRecoveredNode(UpdateMessage umsg, StorageService storageService, ReplicaManager rm){
    	Logger logger = Logger.getLogger("myLogger");
    	logger.debug("Node " + rm.getNodeId() + " receives backup from the RECOVERED node " + umsg.getSenderId());
		rm.setBackupIdTemp(-1);      	
		//It could be the case that the primary node is UP, but this node hasn't received
		// this gossip yet. In this case, I force this knowledge.	
		rm.addMemberToGossipListIfDead(rm.getBackupId());
		storageService.getStorageManager().emptyBackup();  
		rm.partitionUrlsBetweenDB();
		Map<String,Versioned<String>> dump = storageService.getStorageManager().getBackupDump();
         // I also add the removed urls to the backup. They have recognizable because have lurl = null
		for (String surl : rm.getRemoved()) 
			dump.put(surl, null);
		List<UpdateMessage> updates = new ArrayList<>();
		rm.createUpdatesToRecover(updates, dump);
		try {    					
			rm.sendUpdates(rm.getBackupId(), updates);
			rm.unsetRemoved();         
		} catch (UnknownHostException | NullPointerException | InterruptedException
				| ExecutionException e) {
			e.printStackTrace();
			System.out.println("Exception from node " + storageService.getStorageManager().getNid());
		}
    }
    
}
