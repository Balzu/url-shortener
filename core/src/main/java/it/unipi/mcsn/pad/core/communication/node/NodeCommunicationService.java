package it.unipi.mcsn.pad.core.communication.node;

import java.net.UnknownHostException;
import java.util.List;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipService;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.event.GossipListener;

import it.unipi.mcsn.pad.core.Service;
import it.unipi.mcsn.pad.core.storage.StorageService;
import voldemort.versioning.VectorClock;

public class NodeCommunicationService implements Service{
	
	private GossipService gossipService;
	private NodeCommunicationManager nodeCommManager;
	
	/*private String ipAddress;
	private int gossipPort;
	public String id; //TODO set private
	private int logLevel; 
	private List<GossipMember> gossipMembers;
	private GossipSettings settings;
	private GossipListener listener;
	*/

	
	public NodeCommunicationService(String ipAddress, int gossipPort, String id, int logLevel, 
			 List<GossipMember> gossipMembers, GossipSettings settings,
			 GossipListener listener, VectorClock vc, int nid,
			 StorageService storageService , int nodePort, int virtualInstances, int backupInterval)
					 throws UnknownHostException, InterruptedException {	
		/*
		this.ipAddress = ipAddress;
		this.gossipPort = gossipPort;
		this.id = id;
		this.logLevel = logLevel;
		this.gossipMembers = gossipMembers;
		this.settings = settings;
		this.listener = listener;
		*/
		
	    gossipService = new GossipService(ipAddress, gossipPort, id, logLevel, gossipMembers, settings, listener);		
		nodeCommManager = new NodeCommunicationManager(vc, nid, this, virtualInstances, 
				nodePort,ipAddress, storageService, backupInterval);	
	}

	public GossipService getGossipService() {
		return gossipService;
	}

	public NodeCommunicationManager getCommunicationManager() {
		return nodeCommManager;
	}
	

	@Override
	public void start() {			
		gossipService.start();
		nodeCommManager.start();		
	}

	@Override
	public void shutdown() {
		gossipService.shutdown();
		nodeCommManager.shutdown();		
	}		

}
