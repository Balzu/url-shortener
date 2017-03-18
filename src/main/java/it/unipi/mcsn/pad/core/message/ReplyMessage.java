package it.unipi.mcsn.pad.core.message;

public class ReplyMessage implements Message{
	
	//TODO: solo un primo abbozzo
	private String longUrl;
	private String shortUrl;
	
	protected MessageStatus messageStatus;
	protected MessageType messageType;

	public ReplyMessage (String lu, String su, MessageStatus ms){
		messageType=MessageType.REPLY;
		longUrl = lu;
		shortUrl = su;
		messageStatus = ms;
	}
	
	public MessageStatus getMessageStatus() {	
		return messageStatus;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	
	
	@Override
	public String toString() {
		return "url = " + this.longUrl + ", \n shortened_url = " + this.shortUrl; 
	}
	
	

}
