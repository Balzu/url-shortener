package it.unipi.mcsn.pad.core.message;

public class PullMessage extends Message{
	
	public PullMessage (){
		messageType=MessageType.PULL;
	}

}
