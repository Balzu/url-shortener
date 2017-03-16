package it.unipi.mcsn.pad.core.communication.node;

import java.net.UnknownHostException;
import java.util.List;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipService;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.event.GossipListener;

import it.unipi.mcsn.pad.core.Service;

public class NodeCommunicationService implements Service{
	
	private GossipService gossipService;
	private ReplicaManager replicaManager;
	private RequestManager requestManager;
	
	public NodeCommunicationService(String ipAddress, int port, String id, int logLevel, 
			 List<GossipMember> gossipMembers, GossipSettings settings, GossipListener listener) throws UnknownHostException, InterruptedException {
		
		gossipService = new GossipService(ipAddress, port, id, logLevel, gossipMembers, settings, listener);
		gossipService.start();
		replicaManager = new ReplicaManager();
		requestManager = new RequestManager();
	}

	public GossipService getGossipService() {
		return gossipService;
	}

	public ReplicaManager getReplicaManager() {
		return replicaManager;
	}

	public RequestManager getRequestManager() {
		return requestManager;
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
