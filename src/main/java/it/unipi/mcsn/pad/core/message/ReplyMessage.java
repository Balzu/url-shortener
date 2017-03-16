package it.unipi.mcsn.pad.core.message;

public class ReplyMessage extends Message{
	
	//TODO: solo un primo abbozzo
	private String longUrl;
	private String shortUrl;

	
	public ReplyMessage (String lu, String su, MessageStatus ms){
		messageType=MessageType.REPLY;
		longUrl = lu;
		shortUrl = su;
		messageStatus = ms;
	}
	
	@Override
	public String toString() {
		return "url = " + this.longUrl + ", \n shortened_url = " + this.shortUrl; 
	}
	
	

}
