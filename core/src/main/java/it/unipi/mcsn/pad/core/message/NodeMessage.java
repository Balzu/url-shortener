package it.unipi.mcsn.pad.core.message;

import voldemort.versioning.VectorClock;
import voldemort.versioning.Versioned;

public interface NodeMessage extends Message{
	
	public String getShortUrl();
	
	public String getLongUrl();
	
	public VectorClock getVectorClock();
	
	public Versioned<String> getVersioned();
	

}
