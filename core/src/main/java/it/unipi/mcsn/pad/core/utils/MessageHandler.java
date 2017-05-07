package it.unipi.mcsn.pad.core.utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import it.unipi.mcsn.pad.core.communication.node.ReplicaManager;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;
import it.unipi.mcsn.pad.core.storage.StorageManager;
import it.unipi.mcsn.pad.core.storage.StorageService;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Version;
import voldemort.versioning.Versioned;

public class MessageHandler {
	
	
	public static Message handleMessage(Message msg, StorageService storageService, ReplicaManager rm){
		//TODO merge received vector clock with node's vector clock?
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
		    //storageService.getStorageManager().removeBackup(surl);
    	return (new VersionedMessage(lurl, surl, vers,
				MessageType.REPLY, MessageStatus.SUCCESS));
	}
    
    private static NodeMessage processUpdateMessage(
    		UpdateMessage umsg, StorageService storageService, ReplicaManager rm)
    {    	
        // This can only happen if this node has recovered from crashing and now it is receiving
    	// the updates from the backup node
    	if (umsg.getSenderId() == rm.findBackup())    	
    		return processUpdatesFromBackupNode(umsg,storageService,rm);
    	
    	// If the replica manager never received a backup before, we store the id of the sending node in the
    	// replica manager to mean that from now on the replica manager expects update messages from that node
    	if (rm.getBackupId() == -1)
    		rm.setBackupId(umsg.getSenderId());
    	// If the id of the sender is different from the id stored in the replica manager it means that 
    	// a node crashed. The replica manager must prepare 
    	// to receive the following messages from a different node: so it updates its backupIdTemp 
    	// to account for the new sender, and merges the backup DB into the primary DB (indeed from
    	// now on this node should answer also the queries concerning the crashed node)
    	else 
    		// First time I receive update from a node different from default sender
    		if (umsg.getSenderId() != rm.getBackupId() && rm.getBackupIdTemp() == -1){ 
    		 // System.out.println("EBackupIdTemp " + rm.getBackupIdTemp());
    		  rm.setBackupIdTemp(umsg.getSenderId());
    		  rm.setRemoved();
    		  //storageService.getStorageManager().removeAlsoFromBackup(rm.getRemoved());
    		  storageService.getStorageManager().mergeDB();
    		//System.out.println("Actual sender " + umsg.getSenderId());
    		//System.out.println("Expected sender " + rm.getBackupId());
    		  
    	    }
    		else if (umsg.getSenderId() == rm.getBackupId()){  // The sender of the backup is who I expect
    			if(rm.getBackupIdTemp() != -1){ // true IFF sender recovered from crashing
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
    				rm.createUpdates(updates, dump);
    				try {    				
    					// rm.addMemberToGossipListIfDead(rm.getBackupId());
						rm.sendUpdates(rm.getBackupId(), updates);
						rm.unsetRemoved();
// End here: not going to store in the backup DB the outdated url from the crashed node						
						return new VersionedMessage(   
								null, null, null, MessageType.REPLY, MessageStatus.SUCCESS);
					} catch (UnknownHostException | NullPointerException | InterruptedException
							| ExecutionException e) {
						e.printStackTrace();
						System.out.println("Exception from node " + storageService.getStorageManager().getNid());
					}  
    				//TODO put a return here?
    			}
    		}    		
    	
    	// The backup database is emptied every time a sequence of updates is started in order to keep it 
    	// updated.
    	if (umsg.isFirst())
    		storageService.getStorageManager().emptyBackup();    	
    	
    	boolean stored = storageService.getStorageManager().storeBackup(umsg.getitems());
    	if (!stored)
    		return new VersionedMessage(null, null, null, MessageType.REPLY, MessageStatus.ERROR);
    	return new VersionedMessage(null, null, null, MessageType.REPLY, MessageStatus.SUCCESS);
    }
    
    
    private static NodeMessage processUpdatesFromBackupNode(
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
    	return new VersionedMessage(null, null, null, MessageType.REPLY, MessageStatus.SUCCESS);
    }
    
}
