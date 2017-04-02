package it.unipi.mcsn.pad.core.storage;

import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;


import it.unipi.mcsn.pad.core.message.NodeMessage;
import voldemort.versioning.Versioned;

public class StorageManager extends Thread{
	
	private ConcurrentMap <String, Versioned<String>> map ;
	private DB db;
	
	
	public StorageManager() {
		db = DBMaker.fileDB("file.db").fileMmapEnable().make();
		map = (ConcurrentMap<String, Versioned<String>>) db.hashMap("map").createOrOpen();
		
	}
	
	
	
	
	public void store(NodeMessage msg)
	{
		String surl = msg.getShortUrl();
		Versioned<String> versioned = msg.getVersioned();
		map.put(surl, versioned);
		db.commit();
	}
	
	public Versioned<String> read(String surl)
	{
		return map.get(surl);
		
	}
	
	
	
	@Override
	public void run(){
		
	}
	
	public void shutdown(){
		db.close();
	}

}
