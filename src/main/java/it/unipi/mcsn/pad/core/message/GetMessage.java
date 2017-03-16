package it.unipi.mcsn.pad.core.message;

public class GetMessage extends Message{
	
	//TODO: solo un primo abbozzo
	private String shortUrl;
	
	public GetMessage (String url){
		messageType=MessageType.GET;
		shortUrl = url;
	}

}
