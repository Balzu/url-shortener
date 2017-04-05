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
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.ReplyMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;
import it.unipi.mcsn.pad.core.utils.MessageHandler;
import it.unipi.mcsn.pad.core.utils.Partitioner;
import it.unipi.mcsn.pad.core.utils.Utils;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Version;


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
			int nodePort, String ipAddress, StorageService ss) 
	{
		this(vt, nid, nodeCommService,700, ConsistentHasher.SHA1, nodePort,
				ipAddress, ss);
	}
	
	public NodeCommunicationManager(VectorClock vt, int nid, NodeCommunicationService nodeCommService, 
			final int virtualInstancesPerBucket, final HashFunction hashFunction, int nodePort,
			String ipAddress, StorageService ss)
	{
		nodeServiceRunning = new AtomicBoolean(true);
		nodeCommunicationService = nodeCommService;		
		replicaManager = new ReplicaManager();
		partitioner = new Partitioner<>(virtualInstancesPerBucket,
				ConsistentHasher.getIntegerToBytesConverter(), 
				ConsistentHasher.getStringToBytesConverter(), hashFunction);
		vectorClock = vt;
		nodeId = nid;
		storageService = ss;
		try {
			requestManager = new RequestManager(nodeServiceRunning, nodePort,
					ipAddress, storageService);
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
	
	
	
	/**
	 * Processes the message: finds the primary node and sends the message to it
	 * if primary node is different from this node.
	 *  Returns a message containing the reply for the operation.
	 */
	public Message processClientMessage(Message msg) {
		// when receive something, update the vector clock 
		// TODO: (When I receive a msg, should I also merge the vt?)
		vectorClock.incrementVersion(nodeId, System.currentTimeMillis());			
		ClientMessage clmsg = (ClientMessage) msg;		
		String surl = getShortUrl(clmsg);		
		int primaryId = findPrimary(surl); 
		if (primaryId == nodeId){
			//TODO: call HandleMessage and maybe switch on REMOVE, PUT, GET
			MessageHandler.handleMessage(createNodeMessage(clmsg, surl), storageService);
			return null; //TODO return the message
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
	private GossipMember getMemberFromId (int id)
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
	
	// RemoveMessage contains the shortened url; the other kinds of messages contain long url	
	// and so in that case the shortened url has to be generated
	private String getShortUrl(ClientMessage clmsg)
	{
		String surl = null;
		if (clmsg.getMessageType() == MessageType.REMOVE)
			surl = clmsg.getUrl();
		else
			surl=Utils.generateShortUrl(clmsg.getUrl());	
		return surl;
	}

}
