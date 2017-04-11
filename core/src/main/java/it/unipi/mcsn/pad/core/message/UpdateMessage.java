package it.unipi.mcsn.pad.core.message;

import java.util.Map;


import voldemort.versioning.Versioned;

public interface UpdateMessage extends Message{
	
	public boolean isFull();
	
	public boolean isEmpty();
	
	public void put (String surl, Versioned<String> vlurl);
	
	public Map<String,Versioned<String>> getitems();
	
	

}
