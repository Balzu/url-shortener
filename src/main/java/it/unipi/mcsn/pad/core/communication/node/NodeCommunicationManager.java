package it.unipi.mcsn.pad.core.communication.node;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.LocalGossipMember;

import it.unipi.mcsn.pad.consistent.ConsistentHasher;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.HashFunction;
import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;
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
	
	
	public NodeCommunicationManager(VectorClock vt, int nid, NodeCommunicationService nodeCommService, int nodePort) 
	{
		this(vt, nid, nodeCommService,700, ConsistentHasher.SHA1, nodePort);
	}
	
	public NodeCommunicationManager(VectorClock vt, int nid, NodeCommunicationService nodeCommService, 
			final int virtualInstancesPerBucket, final HashFunction hashFunction, int nodePort)
	{
		nodeServiceRunning = new AtomicBoolean(true);
		nodeCommunicationService = nodeCommService;		
		replicaManager = new ReplicaManager();
		partitioner = new Partitioner<>(virtualInstancesPerBucket,
				ConsistentHasher.getIntegerToBytesConverter(), 
				ConsistentHasher.getStringToBytesConverter(), hashFunction);
		vectorClock = vt;
		nodeId = nid;
		try {
			requestManager = new RequestManager(nodeServiceRunning, nodePort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
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
		String surl = Utils.generateShortUrl(clmsg.getLongUrl());		
		int primaryId = findPrimary(surl); 
		if (primaryId == nodeId){
			//TODO: call HandleMessage and maybe switch on REMOVE, PUT, GET
		}
		else {
			// TODO Version message and send it to primary (PROBLEM: retrieve it from nodeId)
			NodeMessage nmsg = new VersionedMessage(clmsg.getLongUrl(), surl, vectorClock);
			GossipMember member = getMemberFromId(primaryId);
			String ipAddr = member.getHost();
			int port = member.getPort();			
			Message reply = requestManager.sendMessage(nmsg, ipAddr, port);
			return reply;
		}
		
		
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

}
