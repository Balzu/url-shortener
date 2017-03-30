package it.unipi.mcsn.pad.core.communication.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unipi.mcsn.pad.core.message.Message;

public class RequestManager extends Thread{
	
	private DatagramSocket socket = null;
	private AtomicBoolean isRunning;
	private final ExecutorService threadPool;
	private int port;
	
	public RequestManager (AtomicBoolean isRunning, int port) throws SocketException{
		socket = new DatagramSocket(port);
		this.isRunning = isRunning;
		this.port = port;
		threadPool = Executors.newCachedThreadPool();
	}
	
	@Override
	public void run()
	{
		while (isRunning.get())
		{
			try {
				// In questo caso do' dimensione massima, ma meglio se lo dico io quanto allocare (?)
				byte[] buf = new byte[socket.getReceiveBufferSize()];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            socket.receive(packet);
	            threadPool.submit(new RequestServerThread(packet, this, socket));				
			} catch (SocketException e) {			
				e.printStackTrace();
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
		
	}
	
	
	//TODO check that putting this method here is ok
	public Message sendMessage(Message msg, String ipAddr, int port) throws UnknownHostException, InterruptedException, ExecutionException
	{		
		Future <Message> future = threadPool.submit(new RequestClientThread(msg, 
				InetAddress.getByName(ipAddr), port));
		return future.get();
		
	}
	
	
	

}
