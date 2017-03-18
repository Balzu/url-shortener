package it.unipi.mcsn.pad.core.communication.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.utils.Partitioner;
import voldemort.versioning.VectorClock;

public class ClientCommunicationManager extends Thread{
	
	//private ClientCommunicationThread clientCommunicationThread;
	private ServerSocket serverSocket;
	private final ExecutorService threadPool;
	//Usato per gestire richieste concorrenti da client (è lock-free), ma va bene il nome??
	private final AtomicBoolean clientServiceRunning; 
	private VectorClock vectorClock;
	private int nodeId;
	
	public ClientCommunicationManager( int port, int backlog, InetAddress bindAddr
			, VectorClock vc, int nid){
		
		clientServiceRunning = new AtomicBoolean(true);
		threadPool = Executors.newCachedThreadPool();
		vectorClock = vc;
		nodeId = nid;
		try { //TODO: Have to close ServerSocket somewhere?
			serverSocket = new ServerSocket(port, backlog, bindAddr);			
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (clientServiceRunning.get()) {
			try {
				Socket clientSocket = serverSocket.accept();
				threadPool.submit(new ClientCommunicationThread(clientSocket, this));				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}
	
	public Message processMessage(Message msg) {
		//when receive something, update the vector clock
		vectorClock.incrementVersion(nodeId, System.currentTimeMillis());
		ClientMessage<String> clmsg = (ClientMessage<String>) msg;
		Partitioner.findPrimary(clmsg.getKey());
	}
	

}
