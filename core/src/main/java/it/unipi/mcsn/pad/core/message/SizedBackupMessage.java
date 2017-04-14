package it.unipi.mcsn.pad.core.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.omg.PortableInterceptor.SUCCESSFUL;

import voldemort.versioning.Versioned;

public class SizedBackupMessage implements UpdateMessage{
	
	private static final long serialVersionUID = 1L;
	
	private int size;
	private int capacity;
	private int senderId;
	private boolean first;
	private Map<String,Versioned<String>> items;
	private MessageStatus messageStatus;
	private MessageType messageType;
	
	public SizedBackupMessage (int senderId, boolean first) {
		this(50,  senderId, first, MessageStatus.SUCCESS);
	}
	
	public SizedBackupMessage (int capacity, int senderId, boolean first) {
		this(capacity, senderId, first, MessageStatus.SUCCESS);
	}

	public SizedBackupMessage (int capacity, int senderId, boolean first, MessageStatus ms)
	{
		messageType=MessageType.UPDATE;		
		messageStatus = ms;
		this.capacity = capacity;
		size = 0;
		this.senderId = senderId;
		this.first = first;
		items = new HashMap<>(capacity);
	}
	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	public void put (String surl, Versioned<String> vlurl){
		items.put(surl, vlurl);
		size++;
	}
	
	public Map<String,Versioned<String>> getitems(){
		return items;
	}

	@Override
	public boolean isFull() {		
		return (size == capacity);
	}	
	
	public boolean isEmpty() {
		return (size == 0);
	}

	@Override
	public int getSenderId()
	{
		return senderId;
	}

	@Override
	
	public boolean isFirst() {
		return first;
	}
	
	
	
	

}
