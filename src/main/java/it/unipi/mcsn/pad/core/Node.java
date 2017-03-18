package it.unipi.mcsn.pad.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.event.GossipListener;

import it.unipi.mcsn.pad.core.communication.client.ClientCommunicationService;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationService;
import it.unipi.mcsn.pad.core.storage.StorageService;
import voldemort.versioning.VectorClock;


public class Node {
	
	private NodeCommunicationService nodeCommService;
	private ClientCommunicationService clientCommService;
	private StorageService storageService;
	private VectorClock vectorClock;
	private int nodeId;
	
	
	public Node (int clientPort, int backlog,	String ipAddress,
			int nodePort, String id, int logLevel, List<GossipMember> gossipMembers,
			GossipSettings settings, GossipListener listener, int nid) throws UnknownHostException, InterruptedException 
	{		
		nodeCommService = new NodeCommunicationService(
				ipAddress, nodePort, id, logLevel, gossipMembers, settings, listener, vectorClock);
		 InetAddress bindAddr = InetAddress.getByName(ipAddress); 
		clientCommService = new ClientCommunicationService(clientPort,  backlog,  bindAddr, vectorClock, nid);
		storageService = new StorageService(vectorClock);	
		nodeId = nid; //TODO: forse puoi ricavere un id unico dalla stringa 'id'
		vectorClock = new VectorClock(); //TODO: ok this constructor?
		vectorClock.incrementVersion(nodeId, System.currentTimeMillis());
	}


	public NodeCommunicationService getNodeCommService()
	{
		return nodeCommService;
	}


	public ClientCommunicationService getClientCommService() 
	{
		return clientCommService;
	}


	public StorageService getStorageService() {
		return storageService;
	}
	
	
	public void start()
	{		
		nodeCommService.start();
		clientCommService.start();
		storageService.start();
	}
	
	public void shutdown()
	{		
		nodeCommService.shutdown();
		clientCommService.shutdown();
		storageService.shutdown();
	}
	
	
	
	

}
