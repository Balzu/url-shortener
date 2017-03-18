package it.unipi.mcsn.pad.core.message;

public class PutMessage implements Message{
	
	//TODO: solo un primo abbozzo
	private String longUrl;
	protected MessageStatus messageStatus;
	protected MessageType messageType;

	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	public PutMessage (String url){
		messageType=MessageType.PUT;
		longUrl = url;
	}

	public String getLongUrl() {
		return longUrl;
	}

}
