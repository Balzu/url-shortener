package it.unipi.mcsn.pad.core.message;

import java.io.Serializable;


public interface Message extends Serializable{	
	
	public MessageStatus getMessageStatus();

	public MessageType getMessageType();
	
}
