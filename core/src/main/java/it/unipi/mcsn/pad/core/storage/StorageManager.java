package it.unipi.mcsn.pad.core.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import it.unipi.mcsn.pad.core.message.NodeMessage;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Versioned;

// The backup DB is never used to answer to queries. If this node has to answer queries concerning items in the
// backup, the system will automatically copy the backup into the primary DB, and will use the primary to
// answer such queries.
public class StorageManager {
	
	private ConcurrentMap <String, Versioned<String>> primaryMap ;
	private ConcurrentMap <String, Versioned<String>> backupMap ;
	private DB db;
	private VectorClock vc;	
	
	public StorageManager(String id, VectorClock vc) {
		db = DBMaker.fileDB("file" + id + ".db").fileMmapEnable().make();
		primaryMap = (ConcurrentMap<String, Versioned<String>>) db.hashMap("myMap").createOrOpen();
		backupMap = (ConcurrentMap<String, Versioned<String>>) db.hashMap("backupMap").createOrOpen();
		this.vc = vc;		
	}
	
	

	public boolean store(String surl, Versioned<String> versioned)
	{
		try{			
			primaryMap.put(surl, versioned);			
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
			Versioned<String> read = primaryMap.get(surl);			
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
			Versioned<String> removed = primaryMap.remove(surl);			
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
		for(Entry<String, Versioned<String>> e :primaryMap.entrySet()){
			String surl= e.getKey();
			// I deliberately create a copy of the object, that I will return
			Versioned<String> vlurl = new Versioned<String>(e.getValue().getValue(), e.getValue().getVersion());
			dump.put(surl, vlurl);
		}
		return dump;
	}
	
	private boolean storeBackup(String surl, Versioned<String> versioned)
	{
		try{			
			backupMap.put(surl, versioned);			
			db.commit();
			return true;
		}
		catch(Exception e){
			db.rollback();
			return false;
		}		
	}
	
	public boolean storeBackup(Map<String,Versioned<String>> backup){
		boolean stored;
		for (Entry<String, Versioned<String>> e : backup.entrySet()){
			String surl= e.getKey();
			Versioned<String> vlurl = e.getValue();
			stored = storeBackup(surl, vlurl);
			if (stored == false)
				return false;  //If some insertion goes wrong, return false			
		}
		return true;
	}
	
	/**
	 * Merge the backup database into the primary database of the node
	 */
	public boolean mergeDB() {
		try{
	   	   for(Entry<String, Versioned<String>> e :backupMap.entrySet()){
			   String surl= e.getKey();
			   // I deliberately create a copy of the object, that I will return
			   Versioned<String> vlurl = new Versioned<String>(e.getValue().getValue(), e.getValue().getVersion());
			   primaryMap.put(surl, vlurl);
		   }
	   	return true;
		}   		
		catch(IllegalStateException e){
			return false;
		}
	}
	
	public boolean emptyBackup(){
		try{
			backupMap.clear();
			return true;
		}
		catch (UnsupportedOperationException e){
			return false;
		}
	}
	
	
	public void shutdown(){
		db.close();
	}

}
