package it.unipi.mcsn.pad.core;

import java.net.ServerSocket;

public class ClientCommunicationManager extends Thread{
	
	private ClientCommunicationThread clientCommunicationThread;
	
	public ClientCommunicationManager(ClientCommunicationThread cct){
		clientCommunicationThread = cct ;
	}
	
	@Override
	public void run() {
		
	}
	

}
