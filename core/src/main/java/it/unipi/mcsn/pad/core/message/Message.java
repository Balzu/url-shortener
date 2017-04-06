package it.unipi.mcsn.pad.core.message;

import java.io.Serializable;


public interface Message extends Serializable{	
	
	//private static final long serialVersionUID = 6819795421360558135L;
	
	public MessageStatus getMessageStatus();

	public MessageType getMessageType();
	
}
