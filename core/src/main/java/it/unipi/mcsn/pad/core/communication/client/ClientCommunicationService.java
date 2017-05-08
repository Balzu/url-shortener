package it.unipi.mcsn.pad.core.communication.client;

import java.io.IOException;
import java.net.InetAddress;
import it.unipi.mcsn.pad.core.Service;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationManager;

public class ClientCommunicationService implements Service
{	
	private ClientCommunicationManager clientCommManager;	
	public ClientCommunicationService(int port, int backlog, InetAddress bindAddr,
			int nodeId, NodeCommunicationManager ncm){
		clientCommManager = new ClientCommunicationManager(port, backlog, bindAddr, nodeId, ncm);
	}

	public ClientCommunicationManager getClientCommManager() {
		return clientCommManager;
	}
	
	public void start(){
		clientCommManager.start();
	}

	@Override
	public void shutdown() {
		try {
			clientCommManager.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
