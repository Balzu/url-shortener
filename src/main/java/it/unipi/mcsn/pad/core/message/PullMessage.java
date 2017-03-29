package it.unipi.mcsn.pad.core.message;

public class PullMessage implements Message{
	
	private static final long serialVersionUID = 1L;
	protected MessageStatus messageStatus;
	protected MessageType messageType;

	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	public PullMessage (){
		messageType=MessageType.PULL;
	}

}
