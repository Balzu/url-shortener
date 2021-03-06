package it.unipi.mcsn.pad.core.communication.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;
import it.unipi.mcsn.pad.core.utils.MessageHandler;
import it.unipi.mcsn.pad.core.utils.Utils;

public class RequestServerThread implements Runnable{
	
	 private DatagramPacket packet;
	 private DatagramSocket socket;
	 private StorageService storageService;
	 private ReplicaManager manager;
	 static Logger logger = Logger.getLogger(MessageHandler.class);
	 
	 public  RequestServerThread(DatagramPacket packet, ReplicaManager rm,
			  DatagramSocket socket, StorageService ss) {
		this.packet = packet;
	    manager = rm;
		this.socket = socket;
		storageService = ss;
		PropertyConfigurator.configure("src/main/resources/log4j.properties");		   	
	}

	@Override
	public void run() {
		
		try {
			Message msg = (Message)Utils.deserialize(packet.getData());
			Message reply = null;
			if (msg instanceof NodeMessage){
				NodeMessage nmsg = (NodeMessage) msg;
				reply = MessageHandler.handleMessage(nmsg, storageService, manager);
			}
			else if (msg instanceof UpdateMessage){
				UpdateMessage umsg = (UpdateMessage) msg;
				reply = MessageHandler.handleMessage(umsg, storageService, manager);
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
