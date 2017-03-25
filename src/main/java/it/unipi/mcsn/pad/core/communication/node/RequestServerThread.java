package it.unipi.mcsn.pad.core.communication.node;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RequestServerThread implements Runnable{
	
	 private DatagramPacket packet;
	 
	 
	 public  RequestServerThread(DatagramPacket packet) {
		this.packet = packet;
	}

	@Override
	public void run() {
		
		
		
	}

}
