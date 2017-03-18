package it.unipi.mcsn.pad.core.communication.client;

import java.net.InetAddress;

import it.unipi.mcsn.pad.core.Service;
import voldemort.versioning.VectorClock;

public class ClientCommunicationService implements Service{
	
	private ClientCommunicationManager clientCommManager;
	private VectorClock vectorClock;
	
	public ClientCommunicationService(int port, int backlog, InetAddress bindAddr, VectorClock vc, int nodeId){
		clientCommManager = new ClientCommunicationManager(port, backlog, bindAddr, vc, nodeId);
		vectorClock = vc;
	}

	public ClientCommunicationManager getClientCommManager() {
		return clientCommManager;
	}
	
	public void start(){
		clientCommManager.start();
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	

}
