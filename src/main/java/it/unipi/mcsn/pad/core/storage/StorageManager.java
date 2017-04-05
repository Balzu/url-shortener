package it.unipi.mcsn.pad.core.storage;

import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;


import it.unipi.mcsn.pad.core.message.NodeMessage;
import voldemort.versioning.Versioned;

public class StorageManager extends Thread{
	
	private ConcurrentMap <String, Versioned<String>> map ;
	private DB db;
	
	
	public StorageManager(String id) {
		db = DBMaker.fileDB("file" + id + ".db").fileMmapEnable().make();
		map = (ConcurrentMap<String, Versioned<String>>) db.hashMap("map").createOrOpen();
		
	}
	
	
	
	//TODO Deve ritornare qualcosa, anche un booleano..
	public boolean store(NodeMessage nmsg)
	{
		try{
			String surl = nmsg.getShortUrl();
			Versioned<String> versioned = nmsg.getVersioned();
			map.put(surl, versioned);
			db.commit();
			return true;
		}
		catch(Exception e){
			db.rollback();
			return false;
		}
		
	}
	
	public Versioned<String> read(String surl)
	{
		try{
			Versioned<String> response = map.get(surl);
			db.commit();
			return response; 		
		}
		catch (Exception e) {
			db.rollback();
			return null;
		}
		
	}
	
	
	public boolean remove(NodeMessage nmsg){
		try{
			String surl = nmsg.getShortUrl();
			map.remove(surl);
			db.commit();
			return true;
		}
		catch (Exception e){
			db.commit();
			return false;
		}		
	}
	
	
	@Override
	public void run(){
		
	}
	
	public void shutdown(){
		db.close();
	}

}
