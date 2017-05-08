package it.unipi.mcsn.pad.core.message;

public class GetMessage implements ClientMessage{	
	
	private static final long serialVersionUID = 1L;
	private String shortUrl;
	private MessageStatus messageStatus;
	private MessageType messageType;

	public GetMessage(String url){
		this(url,null);
	}
	
	public GetMessage (String url, MessageStatus ms){
		messageType=MessageType.GET;
		shortUrl = url;
		messageStatus = ms;
	}
	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	@Override
	public String getUrl()
	{
		return shortUrl;
	}
}
