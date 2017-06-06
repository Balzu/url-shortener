package it.unipi.mcsn.pad.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import it.unipi.mcsn.pad.core.message.Message;

public class RandomClient implements Client{
	
	private ClientConfig clientConf;
	
	public RandomClient(ClientConfig cc)
	{
		clientConf = cc;
	}
	
	/*
	 *  Randomly chooses a node to send the request to
	 * */
	private List<InetSocketAddress> chooseRandomNode() {
		
		List<InetSocketAddress> addresses = clientConf.getAddresses();
		Random random = new Random(System.currentTimeMillis());
		Collections.shuffle(addresses, random);
		//int index = random.nextInt(addresses.size());
		return addresses;
	}

	@Override
	public Message sendRequest(Message msg) {
		
		List<InetSocketAddress> addresses = chooseRandomNode();		
		for (InetSocketAddress ipAddr : addresses){
			try (
					Socket socket = new Socket(ipAddr.getHostName(), ipAddr.getPort());			
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());	
					ObjectInputStream ois= new ObjectInputStream(socket.getInputStream());	
					)
				{			
					oos.writeObject(msg);				
					Message receivedMsg = (Message) ois.readObject();			
					if (receivedMsg != null)
						return receivedMsg;
				}
				catch (UnknownHostException e) {	
				} catch (IOException e) {	
				} catch (ClassNotFoundException e) {
				}		
		}		
		return null;
	}

	public ClientConfig getClientConf() {
		return clientConf;
	}

}
