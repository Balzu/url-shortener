package it.unipi.mcsn.pad.core.message;

public class PutMessage<K> implements ClientMessage{
	
	//TODO: solo un primo abbozzo: va bene così o va specificato che ritorni
	// long_url e non una key?
	private static final long serialVersionUID = 1L;
	private String longUrl;
	protected MessageStatus messageStatus;
	protected MessageType messageType;


	@Override
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	@Override
	public MessageType getMessageType() {
		return messageType;
	}
	
	public PutMessage (String url){
		messageType=MessageType.PUT;
		longUrl = url;
	}
	

	@Override
	public String getShortUrl() {		
		return null;
	}

	
	public String getLongUrl() {		
		return longUrl;
	}

}
