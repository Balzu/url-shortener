package it.unipi.mcsn.pad.core;

import com.google.code.gossip.GossipService;

public class NodeCommunicationService {
	
	private GossipService gossipService;
	private ReplicaManager replicaManager;
	private RequestManager requestManager;
	
	public NodeCommunicationService(GossipService gs, 
			ReplicaManager rpm, RequestManager rqm) {
		gossipService = gs;
		replicaManager = rpm;
		requestManager = rqm;
	}

}
