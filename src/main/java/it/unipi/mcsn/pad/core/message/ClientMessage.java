package it.unipi.mcsn.pad.core.message;

public interface  ClientMessage extends Message {
	
	//Non convinto che questa interfaccia sia ok. Fose metti solo un metodo getUrl,
	//e l' url che viene restituito è shorturl o long url a seconda del tipo di
	//messaggio? (get,put,remove.. ?)
	
	public String getShortUrl();
	
	public String getLongUrl();

}
