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
	private int managerPort;

	
	public NodeCommunicationService(String ipAddress, int nodePort, String id, int logLevel, 
			 List<GossipMember> gossipMembers, GossipSettings settings,
			 GossipListener listener, VectorClock vc, int nid, StorageService storageService)
					 throws UnknownHostException, InterruptedException {		
		gossipService = new GossipService(ipAddress, nodePort, id, logLevel, gossipMembers, settings, listener);		
		managerPort=3000; //TODO: crea due costruttori, di cui uno con porta di default(3000) e uno in cui la passi da commandline
		nodeCommManager = new NodeCommunicationManager(vc, nid, this, managerPort, 
				ipAddress, storageService);	
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
