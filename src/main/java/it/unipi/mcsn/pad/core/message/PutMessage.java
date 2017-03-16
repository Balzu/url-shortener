package it.unipi.mcsn.pad.core.message;

public class PutMessage extends Message{
	
	//TODO: solo un primo abbozzo
	private String longUrl;
	
	public PutMessage (String url){
		messageType=MessageType.PUT;
		longUrl = url;
	}

	public String getLongUrl() {
		return longUrl;
	}

}
