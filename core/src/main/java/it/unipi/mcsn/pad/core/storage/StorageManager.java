package it.unipi.mcsn.pad.core.storage;

import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;


import it.unipi.mcsn.pad.core.message.NodeMessage;
import voldemort.versioning.Versioned;

public class StorageManager {
	
	private ConcurrentMap <String, Versioned<String>> map ;
	private DB db;
	
	
	public StorageManager(String id) {
		db = DBMaker.fileDB("file" + id + ".db").fileMmapEnable().make();
		map = (ConcurrentMap<String, Versioned<String>>) db.hashMap("map").createOrOpen();		
	}
	
	
	public boolean store(NodeMessage nmsg)
	{
		try{
			String surl = nmsg.getShortUrl();
			Versioned<String> versioned = nmsg.getVersioned();
			map.put(surl, versioned);
			for (String s: map.keySet())
				System.out.println(s);
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
			Versioned<String> read = map.get(surl);
			for (String s: map.keySet())
				System.out.println(s);
			db.commit();
			return read; 		
		}
		catch (Exception e) {
			db.rollback();
			return null;
		}
		
	}
	
	
	public Versioned<String> remove(NodeMessage nmsg){
		try{
			String surl = nmsg.getShortUrl();
			Versioned<String> removed = map.remove(surl);
			db.commit();
			return removed;
		}
		catch (Exception e){
			db.rollback();
			return null;
		}		
	}
	
	
	public void shutdown(){
		db.close();
	}

}
