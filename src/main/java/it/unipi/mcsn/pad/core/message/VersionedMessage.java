package it.unipi.mcsn.pad.core.message;


import voldemort.versioning.Version;
import voldemort.versioning.Versioned;

public class VersionedMessage implements NodeMessage{
	
	private static final long serialVersionUID = 1L;
	protected MessageStatus messageStatus;
	protected MessageType messageType;
	private String shortUrl;
	private Versioned<String> versioned;
	
	public  VersionedMessage(String lUrl, String sUrl, Version vectorClock) {
		versioned = new Versioned<String>(lUrl, vectorClock);	
		shortUrl = sUrl;		
	}

	@Override
	public MessageStatus getMessageStatus() 
	{		
		return messageStatus;
	}

	@Override
	public MessageType getMessageType() 
	{		
		return messageType;
	}

	@Override
	public Version getVectorClock() {
		
		return versioned.getVersion();
	}

	@Override
	public String getShortUrl() {
		
		return shortUrl;
	}

	@Override
	public String getLongUrl() {
		
		return ((String )versioned.getValue());
	}

	public Versioned<String> getVersioned() {
		return versioned;
	}
	
	

}
