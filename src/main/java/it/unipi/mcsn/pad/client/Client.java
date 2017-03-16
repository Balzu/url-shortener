package it.unipi.mcsn.pad.client;

import it.unipi.mcsn.pad.core.message.Message;

public interface Client {
	
	/*
	 *  Sends a request to a node of the cluster. Different Client implementations
	 *  can use different policies to choose the node.
	 * */
	public Message sendRequest(Message msg);

}
