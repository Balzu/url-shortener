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
	//private VectorClock vectorClock;
	//private int nodeId;
	
	
	public Node (int clientPort, int backlog,	String ipAddress,
			int gossipPort, String sid, int logLevel, List<GossipMember> gossipMembers,
			GossipSettings settings, GossipListener listener, int iid, int nodePort,
			int virtualInstances, int backupInterval) 
					throws UnknownHostException, InterruptedException 
	{		
		//nodeId = iid; 
		VectorClock vc = new VectorClock(); //TODO: ok this constructor?
		vc.incrementVersion(/*nodeId*/ iid, System.currentTimeMillis());
		storageService = new StorageService(vc, sid);	
		nodeCommService = new NodeCommunicationService(ipAddress, gossipPort, sid,
				logLevel, gossipMembers, settings, listener, vc, iid,
				storageService, nodePort, virtualInstances, backupInterval);
		 InetAddress bindAddr = InetAddress.getByName(ipAddress); 
		clientCommService = new ClientCommunicationService(clientPort,  backlog,  bindAddr, 
				iid, nodeCommService.getCommunicationManager());
	
		
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
