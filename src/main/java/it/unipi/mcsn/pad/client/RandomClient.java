package it.unipi.mcsn.pad.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import it.unipi.mcsn.pad.core.message.Message;

public class RandomClient implements Client{
	
	private ClientConfig clientConf;
	
	public RandomClient(ClientConfig cc) {
		clientConf = cc;
		
	}
	
	/*
	 *  Randomly chooses a node to send the request
	 * */
	private InetSocketAddress chooseRandomNode() {
		
		List<InetSocketAddress> addresses = clientConf.getAddresses();
		Random random = new Random();
		int index = random.nextInt(addresses.size());
		return addresses.get(index);
	}

	@Override
	public Message sendRequest(Message msg) {
		
		InetSocketAddress ipAddr = chooseRandomNode();
		try (
			Socket socket = new Socket(ipAddr.getHostName(), ipAddr.getPort());			
					
			)
		{
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());	
			oos.writeObject(msg);			
			ObjectInputStream ois= new ObjectInputStream(socket.getInputStream());
			Message receivedMsg = (Message) ois.readObject();
			ois.close();
			oos.close();
			return receivedMsg;
			
			
		} catch (UnknownHostException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}		
		return null;
	}

	public ClientConfig getClientConf() {
		return clientConf;
	}

}
