package it.unipi.mcsn.pad.core.utils;

import it.unipi.mcsn.pad.core.communication.node.ReplicaManager;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;
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
				  return processRemoveMessage(nmsg, storageService);
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
    
    private static NodeMessage processUpdateMessage(
    		UpdateMessage umsg, StorageService storageService, ReplicaManager rm)
    {
    	// If the replica manager never received a backup before, we store the id of the sending node in the
    	// replica manager to mean that from now on the replica manager expects update messages from that node
    	if (rm.getBackupId() == -1)
    		rm.setBackupId(umsg.getSenderId());
    	// If the id of the sender is different from the id stored in the replica manager it means that either
    	// a node crashed or a node has recovered from crashing. In both cases the replica manager must prepare 
    	// to receive the following messages from a different node: so it updates its backupId to account for the
    	// new sender, and merges the backup DB into the primary DB (indeed in case the old sender crashed, from
    	// now on this node should answer also the queries concerning the crashed node)
    	else if (umsg.getSenderId() != rm.getBackupId()){
    		rm.setBackupId(umsg.getSenderId());
    		storageService.getStorageManager().mergeDB();
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
}
