package it.unipi.mcsn.pad.core.communication.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationManager;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationManager.MessageTypeException;
import it.unipi.mcsn.pad.core.message.Message;

public class ClientCommunicationManager extends Thread
{	
	private ServerSocket serverSocket;
	private final ExecutorService threadPool;	
	private final AtomicBoolean clientServiceRunning; 	
	private final int nodeId;
	private NodeCommunicationManager nodeCommManager;
	
	public ClientCommunicationManager( int port, int backlog, InetAddress bindAddr,
			int nid, NodeCommunicationManager ncm){
		
		clientServiceRunning = new AtomicBoolean(true);
		threadPool = Executors.newCachedThreadPool();		
		nodeId = nid;
		nodeCommManager = ncm;
		try { 
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
	public Message processMessage(Message msg)
	{		
		Message response= null;
		try {
			response = nodeCommManager.processClientMessage(msg);
		} catch (MessageTypeException e) {
			e.printStackTrace();
		}		
		return response;
	}	

}
