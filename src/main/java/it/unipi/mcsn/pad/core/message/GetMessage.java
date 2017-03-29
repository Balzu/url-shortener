package it.unipi.mcsn.pad.core.message;

public class GetMessage implements Message{
	
	//TODO: solo un primo abbozzo
	private static final long serialVersionUID = 1L;
	private String shortUrl;
	protected MessageStatus messageStatus;
	protected MessageType messageType;

	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	public GetMessage (String url){
		messageType=MessageType.GET;
		shortUrl = url;
	}

}
