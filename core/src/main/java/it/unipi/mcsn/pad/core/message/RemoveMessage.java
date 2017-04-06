package it.unipi.mcsn.pad.core.message;

public class RemoveMessage implements ClientMessage{
	
	private static final long serialVersionUID = 1L;
	private MessageStatus messageStatus;
	private MessageType messageType;
	private String shortUrl;

	public RemoveMessage(String url){
		this(url,null);
	}
	
	public RemoveMessage (String url, MessageStatus ms){
		messageType=MessageType.REMOVE;
		shortUrl = url;
		messageStatus = ms;
	}
	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	public RemoveMessage (){
		messageType=MessageType.REMOVE;
	}

	@Override
	public String getUrl() 
	{
		return shortUrl;
	}

}
