package it.unipi.mcsn.pad.core.communication.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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
				byte[] buf = new byte[socket.getReceiveBufferSize()];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            socket.receive(packet);
	            threadPool.submit(new RequestServerThread(port));				
			} catch (SocketException e) {			
				e.printStackTrace();
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
		
	}

}
