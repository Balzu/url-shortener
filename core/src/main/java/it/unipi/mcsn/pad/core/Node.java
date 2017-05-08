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
	
	// Following instance members only used to restart the service
	private int clientPort;
	private int backlog;
	private String ipAddress;
	private int gossipPort;
	private String sid;
	private int logLevel;
	private List<GossipMember> gossipMembers;
	private GossipSettings settings;
	private GossipListener listener;
	private int iid; 
	private int nodePort;
	private int virtualInstances;
	private int backupInterval;
	private VectorClock vc;
	
	
	public Node (int clientPort, int backlog,	String ipAddress,
			int gossipPort, String sid, int logLevel, List<GossipMember> gossipMembers,
			GossipSettings settings, GossipListener listener, int iid, int nodePort,
			int virtualInstances, int backupInterval) 
					throws UnknownHostException, InterruptedException 
	{		
		this.clientPort = clientPort;
		this.backlog = backlog;
		this.ipAddress = ipAddress;
		this.gossipPort = gossipPort;
		this.sid = sid;
		this.logLevel = logLevel;
		this.gossipMembers = gossipMembers;
		this.settings = settings;
		this.listener = listener;
		this.iid = iid;
		this.nodePort = nodePort;
		this.virtualInstances = virtualInstances;
		this.backupInterval = backupInterval;
		vc = new VectorClock(); 
		vc.incrementVersion(iid, System.currentTimeMillis());
		
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
	
		
	public void restart() throws UnknownHostException, InterruptedException
	{		
		storageService = new StorageService(vc, sid);	
		nodeCommService = new NodeCommunicationService(ipAddress, gossipPort, sid,
				logLevel, gossipMembers, settings, listener, vc, iid,
				storageService, nodePort, virtualInstances, backupInterval);
		 InetAddress bindAddr = InetAddress.getByName(ipAddress); 
		clientCommService = new ClientCommunicationService(clientPort,  backlog,  bindAddr, 
				iid, nodeCommService.getCommunicationManager());
		nodeCommService.start();
		clientCommService.start();
		storageService.start();		
		
	}
}
