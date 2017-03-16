package it.unipi.mcsn.pad.core.communication.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientCommunicationManager extends Thread{
	
	//private ClientCommunicationThread clientCommunicationThread;
	private ServerSocket serverSocket;
	private final ExecutorService threadPool;
	//Usato per gestire richieste concorrenti da client (è lock-free), ma va bene il nome??
	private final AtomicBoolean clientServiceRunning; 
	
	public ClientCommunicationManager( int port, int backlog, InetAddress bindAddr ){
		
		clientServiceRunning = new AtomicBoolean(true);
		threadPool = Executors.newCachedThreadPool();
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
				threadPool.submit(new ClientCommunicationThread(clientSocket));				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
		}
		
		
		
	}
	

}
