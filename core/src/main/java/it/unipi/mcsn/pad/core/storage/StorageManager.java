package it.unipi.mcsn.pad.core.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import it.unipi.mcsn.pad.core.message.NodeMessage;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Versioned;

public class StorageManager {
	
	private ConcurrentMap <String, Versioned<String>> map ;
	private DB db;
	private VectorClock vc;
	
	
	public StorageManager(String id, VectorClock vc) {
		db = DBMaker.fileDB("file" + id + ".db").fileMmapEnable().make();
		map = (ConcurrentMap<String, Versioned<String>>) db.hashMap("map").createOrOpen();
		this.vc = vc;		
	}
	
	

	public boolean store(String surl, Versioned<String> versioned)
	{
		try{			
			map.put(surl, versioned);			
			db.commit();
			return true;
		}
		catch(Exception e){
			db.rollback();
			return false;
		}		
	}
	
	public boolean store(NodeMessage nmsg)
	{
		String surl = nmsg.getShortUrl();
		Versioned<String> versioned = nmsg.getVersioned();
		return store(surl, versioned);
	}
	
	
	public Versioned<String> read(String surl)
	{
		try{
			Versioned<String> read = map.get(surl);			
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
	
	//TODO: check if thread-safe
	/**	 
	 * @return a dump of the local database
	 */
	public Map<String,Versioned<String>> getDump() 
	{
		Map<String, Versioned<String>> dump = new HashMap<>();
		for(Entry<String, Versioned<String>> e :map.entrySet()){
			String surl= e.getKey();
			// I deliberately create a copy of the object, that I will return
			Versioned<String> vlurl = new Versioned<String>(e.getValue().getValue(), e.getValue().getVersion());
			dump.put(surl, vlurl);
		}
		return dump;
	}
	
	public boolean storeBackup(Map<String,Versioned<String>> backup){
		boolean stored;
		for (Entry<String, Versioned<String>> e : backup.entrySet()){
			String surl= e.getKey();
			Versioned<String> vlurl = e.getValue();
			stored = store(surl, vlurl);
			if (stored == false)
				return false;  //If some insertion goes wrong, return false			
		}
		return true;
	}
	
	
	public void shutdown(){
		db.close();
	}

}
