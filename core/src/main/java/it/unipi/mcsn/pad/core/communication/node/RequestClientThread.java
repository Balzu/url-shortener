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
	
	public RequestClientThread (Message msg, InetAddress addr, int p)
	{
		message = msg;
		ipAddr = addr;
		port = p;
		try {
			clientSocket = new DatagramSocket();
			//clientSocket.setSoTimeout(5000); //Wait at most 5 seconds for the response
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
		}
	    catch (SocketTimeoutException se) {
	    	//TODO: se receive eccede timeout, ritorna messaggio di errore.
	    	// Problema: discriminare il caso in cui è la receive a sollevare questa eccezione,
	    	// e non altri metodi!
	    }		
		byte[] incomingBuffer = new byte [clientSocket.getSendBufferSize()];
		packet = new DatagramPacket(incomingBuffer, incomingBuffer.length);
	    clientSocket.receive(packet);
	    Message msg = (Message) Utils.deserialize(packet.getData());
		return msg;		
	}

}
