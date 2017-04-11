package it.unipi.mcsn.pad.core.communication.node;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.LocalGossipMember;

import it.unipi.mcsn.pad.consistent.ConsistentHasher;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.HashFunction;
import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;

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
	private NodeCommunicationService nodeCommunicationService;
	// I identify the buckets(= nodes) with a unique integer (node id) 
	// and the members(=long_urls) with a String (their content).
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
		partitioner = new Partitioner<>(virtualInstancesPerBucket,
				ConsistentHasher.getIntegerToBytesConverter(), 
				ConsistentHasher.getStringToBytesConverter(), hashFunction);
		vectorClock = vt;
		nodeId = nid;
		storageService = ss;
		
		try {
			requestManager = new RequestManager(nodeServiceRunning, nodePort,
					ipAddress, storageService);
			replicaManager = new ReplicaManager(storageService, 3000, ipAddress, this, nid, backupInterval);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void start(){
		requestManager.start();
		replicaManager.start();
	}
	
	public void shutdown() {
		requestManager.shutdown();
		replicaManager.shutdown();
	}
	
	
	
	/**
	 * Processes the message: finds the primary node and sends the message to it
	 * if primary node is different from this node. 
	 * @param  The message received from client
	 * @return A message containing the reply for the operation  
	 */
	public Message processClientMessage(Message msg) {
		// when receive something, update the vector clock 
		// TODO: (When I receive a msg, should I also merge the vt?)
		vectorClock.incrementVersion(nodeId, System.currentTimeMillis());			
		ClientMessage clmsg = (ClientMessage) msg;		
		String surl = getShortUrl(clmsg);		
		int primaryId = findPrimary(surl); 
		if (primaryId == nodeId){			
			NodeMessage reply = (NodeMessage)MessageHandler.handleMessage(createNodeMessage(clmsg, surl), storageService);
			return reply; 
		}
		else {
			// TODO Version message and send it to primary			
			NodeMessage nmsg = createNodeMessage(clmsg, surl);
			GossipMember member = getMemberFromId(primaryId);
			String ipAddr = member.getHost();
			int port = requestManager.getPort();			
			Message reply=null;
			try {
				reply = requestManager.sendMessage(nmsg, ipAddr, port);
			} catch (UnknownHostException | InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return reply;
		}		
	}	
	
	
	private NodeMessage createNodeMessage(ClientMessage clmsg, String surl)
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
		}	//TODO: handle default case with exception?		
		
		return (new VersionedMessage(lurl, surl, vectorClock, mt));
	}
	
	
	// TODO: forse da spostare in RequestManager
	/** 
	 *  Gets an updated list of active nodes in the cluster, then uses Consistent Hashing
	 *  to discover which is the primary node for the provided key
	 */
	private int findPrimary(String key){
		
		List<LocalGossipMember> members = new ArrayList<>();
		// NO MORE TRUE: I create nodes s.t. String id = Ip address of the node without dots.
		// (Each GossipMember has a String id = ip address of that node. I give an integer representation
		// of the ids, and then I use the int ids to identify the corresponding buckets in the ring.)
		members = nodeCommunicationService.getGossipService().get_gossipManager().getMemberList();
		List<Integer> buckets = new ArrayList<>();
		for (LocalGossipMember member : members){
			//int id = Utils.getIntegerIpAddress(member.getId());
			int id = Integer.parseInt(member.getId());
			buckets.add(id);			
		}
		
		return partitioner.findPrimary(key, buckets);
		
	}
	
	/**
	 * Given the id of the node, returns the corresponding IP address. 
	 */
	public GossipMember getMemberFromId (int id)
	{
		List<LocalGossipMember> members = new ArrayList<>();
		members = nodeCommunicationService.getGossipService().get_gossipManager().getMemberList();
		for (LocalGossipMember member : members){
			if (Integer.parseInt(member.getId()) == id)
				return member;							
		}
		//TODO: raise an exception instead
		return null;			
	}
	
	// REMOVE and GET message contains the short url, so we only have to retrieve it.
	// PUT message instead contains the long url: in this case, the short url has to be generated
	private String getShortUrl(ClientMessage clmsg)
	{
		String surl = null;
		if (clmsg.getMessageType() == MessageType.PUT)			
		    surl=Utils.generateShortUrl(clmsg.getUrl());	
		else
			surl = clmsg.getUrl();
		return surl;
	}
	
	public int getClusterSize(){
		return nodeCommunicationService.getGossipService().get_gossipManager().getMemberList().size()+1;
	}
	
	public RequestManager getRequestManager(){
	 	return requestManager;
	}

}
