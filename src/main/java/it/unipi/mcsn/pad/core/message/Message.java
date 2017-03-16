package it.unipi.mcsn.pad.core.message;

import java.io.Serializable;

public abstract class Message implements Serializable{	
	
	private static final long serialVersionUID = 6819795421360558135L;
	protected MessageStatus messageStatus;
	protected MessageType messageType;

	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	
}
