package it.unipi.mcsn.pad.core.communication.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.utils.Utils;

public class RequestClientThread implements Runnable{
	
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
			clientSocket.setSoTimeout(5000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		byte[] buffer = null;
		try {
			buffer = Utils.serialize(message);			
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddr, port);
			clientSocket.send(packet);			
			byte[] incomingBuffer = new byte [clientSocket.getSendBufferSize()];
			packet = new DatagramPacket(incomingBuffer, incomingBuffer.length);
		    clientSocket.receive(packet);
		    NodeMessage msg = (NodeMessage) Utils.deserialize(packet.getData());
		}
	    catch (SocketTimeoutException se) {
	    	//TODO: se receive eccede timeout, ritorna messaggio di errore.
	    	// Problema: discriminare il caso in cui è la receive a sollevare questa eccezione,
	    	// e non altri metodi!
	    }
		catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
				
		
    
		
	}

}
