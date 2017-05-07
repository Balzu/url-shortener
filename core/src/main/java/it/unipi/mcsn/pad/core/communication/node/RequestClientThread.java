package it.unipi.mcsn.pad.core.communication.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.utils.Utils;

public class RequestClientThread implements Callable{
	
	private Message message;
	private DatagramSocket clientSocket;
	private InetAddress ipAddr;
	private int port;
	int numAttempt;
	
	public RequestClientThread (Message msg, InetAddress addr, int p)
	{
		message = msg;
		ipAddr = addr;
		port = p;
		numAttempt=0;
		try {
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(5000); 
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}


	@Override
	public Object call() throws Exception {
		
		byte[] buffer = null;
		buffer = Utils.serialize(message);			
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddr, port);
		try {			
			clientSocket.send(packet);	
			byte[] incomingBuffer = new byte [clientSocket.getSendBufferSize()];
			packet = new DatagramPacket(incomingBuffer, incomingBuffer.length);
			
		    clientSocket.receive(packet);
		}
	    catch (SocketTimeoutException se) {
	    	/*numAttempt++;
	    	if ( numAttempt < 3)
	    		return call();*/
	    	//System.out.println("Socket closed because of timeout");
	    	return null; //TODO check this
	    }			
	    Message msg = (Message) Utils.deserialize(packet.getData());
		return msg;		
	}

}
