package it.unipi.mcsn.pad.core.communication.node;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.LocalGossipMember;
import com.google.code.gossip.manager.GossipManager;

import it.unipi.mcsn.pad.consistent.ConsistentHasher;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.HashFunction;
import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.SizedBackupMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;
import it.unipi.mcsn.pad.core.utils.MessageHandler;
import it.unipi.mcsn.pad.core.utils.Partitioner;
import it.unipi.mcsn.pad.core.utils.Utils;
import voldemort.versioning.VectorClock;



public class NodeCommunicationManager {
	
	private final AtomicBoolean nodeServiceRunning; 
	private ReplicaManager replicaManager;
	private RequestManager requestManager;
	public NodeCommunicationService nodeCommunicationService;
	// I identify the buckets(= nodes) with a unique integer (node id) 
	// and the members(= urls) with a String (their content).
	private Partitioner<Integer, String> partitioner;
	private VectorClock vectorClock;
	private int nodeId;
	private StorageService storageService;
	
	
	public NodeCommunicationManager(VectorClock vt, int nid, NodeCommunicationService nodeCommService,
			int virtualInstances, int nodePort, String ipAddress, StorageService ss, int backupInterval) 
	{
		this(vt, nid, nodeCommService,virtualInstances, ConsistentHasher.SHA1, nodePort,
				ipAddress, ss, backupInterval);
	}
	
	public NodeCommunicationManager(VectorClock vt, int nid, NodeCommunicationService nodeCommService, 
			final int virtualInstancesPerBucket, final HashFunction hashFunction, int nodePort,
			String ipAddress, StorageService ss, int backupInterval)
	{
		nodeServiceRunning = new AtomicBoolean(true);
		nodeCommunicationService = nodeCommService;			
		List<LocalGossipMember> members = new ArrayList<>(nodeCommService.getGossipService().get_gossipManager().getMemberList());
		members.add(nodeCommService.getGossipService().get_gossipManager().getMyself());
		partitioner = new Partitioner<Integer, String>(virtualInstancesPerBucket,
				ConsistentHasher.getIntegerToBytesConverter(), 
				ConsistentHasher.getStringToBytesConverter(), hashFunction,
				createBuckets(members));
		vectorClock = vt;
		nodeId = nid;
		storageService = ss;		
		
		try {
			replicaManager = new ReplicaManager(storageService, 3000, ipAddress, this, nid, backupInterval);
			requestManager = new RequestManager(nodeServiceRunning, nodePort,
					ipAddress, storageService, replicaManager);			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}		
	}	

	public void start(){
		requestManager.start();
		replicaManager.start();
	}
	
	public void shutdown() {
		try {
			replicaManager.sendBackupDB();
			if (!replicaManager.isLast()){
				informLeaving();
				storageService.getStorageManager().emptyPrimary();
			}			
			requestManager.shutdown();
			replicaManager.shutdown();
		} catch (NullPointerException e) {
			Logger.getLogger("myLogger").info("The last node of the cluster has been shut down");;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}				
	}	
	
	public void shutdownWithFailure() {
		requestManager.shutdown();
		replicaManager.shutdown();
	}
	
	/**
	 * Processes the message: finds the primary node and sends the message to it
	 * if primary node is different from this node. 
	 * @param  The message received from client
	 * @return A message containing the reply for the operation  
	 * @throws MessageTypeException 
	 * @throws Exception 
	 */
	public Message processClientMessage(Message msg) throws MessageTypeException {					
		ClientMessage clmsg = (ClientMessage) msg;		
		String surl = getShortUrl(clmsg);		
		int primaryId = findPrimary(surl); 
		if (primaryId == nodeId){			
			NodeMessage reply = (NodeMessage)MessageHandler.handleMessage(createNodeMessage(clmsg, surl),
					storageService, replicaManager);
			return reply; 
		}
		else {		
			NodeMessage nmsg = createNodeMessage(clmsg, surl);
			GossipMember member = getMemberFromId(primaryId);
			String ipAddr = member.getHost();
			int port = requestManager.getPort();			
			Message reply=null;
			try {
				reply = requestManager.sendMessage(nmsg, ipAddr, port);
			} catch (UnknownHostException | InterruptedException | ExecutionException e) {				
				e.printStackTrace();
			}
			return reply;
		}		
	}	
	
	public class MessageTypeException extends Exception {
	    public MessageTypeException(String message) {
	        super(message);
	    }
	}
	
	private NodeMessage createNodeMessage(ClientMessage clmsg, String surl) throws MessageTypeException
	{
		String lurl = null;
		MessageType mt = null;
		
		//Only PUT message has long_url != null. In any case, have to specify the MessageType
		switch(clmsg.getMessageType())
		{
		  case PUT:
			  lurl = clmsg.getUrl();
			  mt = MessageType.PUT;
			  break;
		  case GET:
			  mt = MessageType.GET;
			  break;		  
		  case REMOVE:
			  mt = MessageType.REMOVE;
			  break;
		default:
			throw new MessageTypeException("Not a valid operation for a client message");							 
		}		
		return (new VersionedMessage(lurl, surl, vectorClock, mt));
	}	
	
	// TODO: made public only for test purposes
	/** 
	 *  Gets an updated list of active nodes in the cluster, then uses Consistent Hashing
	 *  to discover which is the primary node for the provided key
	 */
	public int findPrimary(String key){		
		GossipManager gManager = nodeCommunicationService.getGossipService().get_gossipManager();
		List<LocalGossipMember> members = new ArrayList<>(gManager.getMemberList());
		// Put also myself in the member list, because I can be the primary too
		members.add(gManager.getMyself());
		List<Integer> buckets = createBuckets(members);
		return partitioner.findPrimary(key, buckets);		
	}	
	
	private List<Integer> createBuckets(List<LocalGossipMember> members){
		List<Integer> buckets = new ArrayList<>();
		for (LocalGossipMember member : members){
			int id = Integer.parseInt(member.getId());
			buckets.add(id);			
		}		
		return buckets;
	}
	
	/**
	 * Given the id of the node, returns the corresponding IP address. 
	 */
	public GossipMember getMemberFromId (int id)
	{
		GossipManager gManager = nodeCommunicationService.getGossipService().get_gossipManager();
		List<LocalGossipMember> members = new ArrayList<>(gManager.getMemberList());	
		for (LocalGossipMember member : members){
			if (Integer.parseInt(member.getId()) == id)
				return member;							
		}		
		throw new NullPointerException("The searched member" + 
		"does not belong to the list of active gossip members.");			
	}
	
	// TODO: made public only for test purposes
	// REMOVE and GET message contains the short url, so we only have to retrieve it.
	// PUT message instead contains the long url: in this case, the short url has to be generated
	public String getShortUrl(ClientMessage clmsg)
	{
		String surl = null;
		if (clmsg.getMessageType() == MessageType.PUT)			
		    surl=Utils.generateShortUrl(clmsg.getUrl());	
		else
			surl = clmsg.getUrl();
		return surl;
	}
	
	
	public RequestManager getRequestManager(){
	 	return requestManager;
	}
	
	// Only for test purposes
		public Partitioner<Integer, String> getPartitioner() {
			return partitioner;
		}

		public NodeCommunicationService getNodeCommunicationService() {
			return nodeCommunicationService;
		}		
		
		public void informLeaving() throws NullPointerException, UnknownHostException, InterruptedException, ExecutionException{
			UpdateMessage leave = new SizedBackupMessage(0, nodeId, false, MessageStatus.SUCCESS, MessageType.LEAVE);	
			int replica = replicaManager.findBackup();
			String replicaAddress = getMemberFromId(replica).getHost();
			replicaManager.addMemberToGossipListIfDead(replica);
			int replicaPort = requestManager.getPort();
			UpdateMessage informed = (UpdateMessage)requestManager.sendMessage(leave, replicaAddress, replicaPort);			
			if (informed == null){ //either is the last node in the cluster or the gossip protocol is giving temporary wrong results
				int c = 0;
				while (c < 3 && informed == null){
					replica = replicaManager.findBackup();
					replicaAddress = getMemberFromId(replica).getHost();
					replicaManager.addMemberToGossipListIfDead(replica);
					informed = (UpdateMessage)requestManager.sendMessage(leave, replicaAddress, replicaPort);	
					c++;
				}
			}
		}
}
