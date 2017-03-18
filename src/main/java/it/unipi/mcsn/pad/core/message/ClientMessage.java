package it.unipi.mcsn.pad.core.message;

public interface  ClientMessage<K> extends Message {
	
	public K getKey();

}
