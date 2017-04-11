package it.unipi.mcsn.pad.core.communication.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.PutMessage;


public class ClientCommunicationThread implements Runnable{
	
	private Socket socket;
	private ClientCommunicationManager manager;
	
	public ClientCommunicationThread(Socket sck, ClientCommunicationManager mgr){		
		socket = sck;
		manager = mgr;
	}

	public void run() {		
		try (
			ObjectInputStream ois= new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());				
		)
		{
			Message message = (Message)ois.readObject();
			Message reply = null;
			reply = manager.processMessage(message);
			oos.writeObject(reply);
		} 
		
		catch (IOException e) {					
			e.printStackTrace();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}
		
	}

}
