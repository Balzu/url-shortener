package it.unipi.mcsn.pad.core.communication.node;

import java.net.UnknownHostException;
import java.util.List;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipService;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.event.GossipListener;

import it.unipi.mcsn.pad.core.Service;
import voldemort.versioning.VectorClock;

public class NodeCommunicationService implements Service{
	
	private GossipService gossipService;
	private NodeCommunicationManager nodeCommManager;

	
	public NodeCommunicationService(String ipAddress, int nodePort, String id, int logLevel, 
			 List<GossipMember> gossipMembers, GossipSettings settings,
			 GossipListener listener, VectorClock vc, int nid)
					 throws UnknownHostException, InterruptedException {
		
		gossipService = new GossipService(ipAddress, nodePort, id, logLevel, gossipMembers, settings, listener);
		gossipService.start();
		nodeCommManager = new NodeCommunicationManager(vc, nid, this, nodePort);	
	}

	public GossipService getGossipService() {
		return gossipService;
	}

	public NodeCommunicationManager getCommunicationManager() {
		return nodeCommManager;
	}

	

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	
	

}
