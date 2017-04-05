package it.unipi.mcsn.pad.core.communication.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.ReplyMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;
import it.unipi.mcsn.pad.core.utils.MessageHandler;
import it.unipi.mcsn.pad.core.utils.Utils;

public class RequestServerThread implements Runnable{
	
	 private DatagramPacket packet;
	 private RequestManager manager;
	 private DatagramSocket socket;
	 private StorageService storageService;
	 
	 public  RequestServerThread(DatagramPacket packet, RequestManager rm, 
			  DatagramSocket socket, StorageService ss) {
		this.packet = packet;
		this.manager = rm;
		this.socket = socket;
		storageService = ss;
	}

	@Override
	public void run() {
		
		NodeMessage nmsg = null;
		try {
			nmsg = (NodeMessage) Utils.deserialize(packet.getData());
		} catch (ClassNotFoundException | IOException e2) {			
			e2.printStackTrace();
		}
		
		//TODO: The received message must be processed before Reply can be issued
		
		
		
		NodeMessage reply = MessageHandler.handleMessage(nmsg, storageService);
		//TODO: different message generated based upon outcome of request (SUCCESS, ERROR, ...)
		byte[] serializedReply = null;
		try {
			serializedReply = Utils.serialize(reply);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		InetAddress IPAddress = packet.getAddress();
		int port = packet.getPort();
		packet = new DatagramPacket(serializedReply, serializedReply.length, IPAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		
	}

}
