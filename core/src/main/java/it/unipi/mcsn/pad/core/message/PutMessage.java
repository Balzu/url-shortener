package it.unipi.mcsn.pad.core.message;

public class PutMessage implements ClientMessage{	
	
	private static final long serialVersionUID = 1L;
	private String longUrl;
	private MessageStatus messageStatus;
	private MessageType messageType;

	public PutMessage(String url){
		this(url,null);
	}

	public PutMessage (String url, MessageStatus ms){
		messageType=MessageType.PUT;
		longUrl = url;
		messageStatus = ms;
	}
	
	@Override
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	@Override
	public MessageType getMessageType() {
		return messageType;
	}
		
	@Override
	public String getUrl() {		
		return longUrl;
	}
}
