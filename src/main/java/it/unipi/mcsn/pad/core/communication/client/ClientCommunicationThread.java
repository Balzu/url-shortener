package it.unipi.mcsn.pad.core.communication.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.MessageStatus;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.PutMessage;
import it.unipi.mcsn.pad.core.message.ReplyMessage;

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
			switch(message.getMessageType())
			{
			  case GET:
				  reply = processGetMessage(message);
				  break;
			  case PUT:
				  reply = processPutMessage(message);
				  break;
			  case LIST:
				  reply = processListMessage(message);
				  break;
			  case REMOVE:
				  reply = processRemoveMessage(message);
				  break;
			  default:
				  reply = new ReplyMessage(null,null, MessageStatus.ERROR);
			}	
			
			oos.writeObject(reply);
		} 
		
		catch (IOException e) {					
			e.printStackTrace();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}
		
	}
	
	//TODO spostare i seguenti metodi in classi nuove, che hanno la responsabilità di gestire i messaggi
	private Message processGetMessage(Message msg){
		return null;
	}
	
    private Message processPutMessage(Message msg){
    	PutMessage pmsg = (PutMessage)msg;
    	System.out.println("Processing put message...");
    	Message reply = new ReplyMessage(pmsg.getLongUrl(), "short_url", MessageStatus.SUCCESS);
		return reply;
	}
    
    private Message processListMessage(Message msg){
    	return null;
	}
    
    private Message processRemoveMessage(Message msg){
    	return null;
	}
	

}
