package it.unipi.mcsn.pad.core.communication.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
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
		
		try {
			Message msg = (Message)Utils.deserialize(packet.getData());
			Message reply = null;
			if (msg instanceof NodeMessage){
				NodeMessage nmsg = (NodeMessage) msg;
				reply = MessageHandler.handleMessage(nmsg, storageService);
			}
			else if (msg instanceof UpdateMessage){
				UpdateMessage umsg = (UpdateMessage) msg;
				reply = MessageHandler.handleMessage(umsg, storageService);
			}
			byte[] serializedReply = null;
			serializedReply = Utils.serialize(reply);
			InetAddress IPAddress = packet.getAddress();
			int port = packet.getPort();
			packet = new DatagramPacket(serializedReply, serializedReply.length, IPAddress, port);
			socket.send(packet);
			
		} 
		catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}		
		catch (IOException e) {
			e.printStackTrace();
		}
		

		
	}

}
