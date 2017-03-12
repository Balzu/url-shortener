package it.unipi.mcsn.pad.core;

import it.unipi.mcsn.pad.core.NodeCommunicationService;
import it.unipi.mcsn.pad.core.ClientCommunicationService;
import it.unipi.mcsn.pad.core.StorageService;


public class Node {
	
	private NodeCommunicationService nodeCommService;
	private ClientCommunicationService clientCommService;
	private StorageService storageService;
	
	
	public Node (NodeCommunicationService ncs, 
			ClientCommunicationService ccs, StorageService ss) {
		nodeCommService = ncs;
		clientCommService = ccs;
		storageService = ss;		
	}
	
	
	

}
