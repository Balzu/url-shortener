package it.unipi.mcsn.pad.core.communication.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unipi.mcsn.pad.core.Node;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationManager;
import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.utils.Partitioner;
import voldemort.versioning.VectorClock;

public class ClientCommunicationManager extends Thread{
	
	//private ClientCommunicationThread clientCommunicationThread;
	private ServerSocket serverSocket;
	private final ExecutorService threadPool;
	//Usato per gestire richieste concorrenti da client (è lock-free), ma va bene il nome??
	private final AtomicBoolean clientServiceRunning; 
	
	private final int nodeId;
	private NodeCommunicationManager nodeCommManager;
	
	public ClientCommunicationManager( int port, int backlog, InetAddress bindAddr,
			int nid, NodeCommunicationManager ncm){
		
		clientServiceRunning = new AtomicBoolean(true);
		threadPool = Executors.newCachedThreadPool();
		
		nodeId = nid;
		nodeCommManager = ncm;
		try { //TODO: Have to close ServerSocket somewhere? Think only on shutdown!!
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
				//e.printStackTrace();
			}			
		}		
	}
	
	public void shutdown() throws IOException {
		clientServiceRunning.set(false);
		serverSocket.close();
	}
	
	/**
	 * Delegates the processing of the message to the NodeCommunicationManager.
	 *  Returns a message containing the reply for the operation.
	 */
	public Message processMessage(Message msg) {
		
		Message response = nodeCommManager.processClientMessage(msg);
		
		return response;
	}
	

}
