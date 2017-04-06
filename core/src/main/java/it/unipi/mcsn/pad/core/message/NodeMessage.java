package it.unipi.mcsn.pad.core.message;

import voldemort.versioning.Version;
import voldemort.versioning.Versioned;

public interface NodeMessage extends Message{
	
	public String getShortUrl();
	
	public String getLongUrl();
	
	public Version getVectorClock();
	
	public Versioned<String> getVersioned();
	

}
