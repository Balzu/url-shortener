package it.unipi.mcsn.pad.core.communication.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.storage.StorageService;

public class RequestManager extends Thread{
	
	private DatagramSocket socket = null;
	private AtomicBoolean isRunning;
	private final ExecutorService threadPool;
	private int nodePort;
	private StorageService storageService;
	private ReplicaManager repMan;
	
	public RequestManager (AtomicBoolean isRunning, int nodePort,
			String ipAddress, StorageService ss, ReplicaManager rm) throws SocketException, UnknownHostException{
		socket = new DatagramSocket(nodePort, InetAddress.getByName(ipAddress));
		socket.setSoTimeout(5000);
		this.isRunning = isRunning;
		this.nodePort = nodePort;
		storageService = ss;
		threadPool = Executors.newCachedThreadPool();
		repMan = rm;
	}
	
	@Override
	public void run()
	{
		while (isRunning.get())
		{
			try {				
				byte[] buf = new byte[socket.getReceiveBufferSize()];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            socket.receive(packet);
	            threadPool.submit(new RequestServerThread(packet, repMan, socket, storageService));		            
			} catch (SocketTimeoutException e) {
			}				
			  catch (SocketException e) {
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}		
	}
	
	public void shutdown(){
		isRunning.set(false);
		socket.close();
	}	
	
	public int getPort() {
		return nodePort;
	}
	
	public Message sendMessage(Message msg, String ipAddr, int port) throws UnknownHostException, InterruptedException, ExecutionException
	{		
		Future <Message> future = threadPool.submit(new RequestClientThread(msg, 
				InetAddress.getByName(ipAddr), port));
		return future.get();		
	}	

}
