package it.unipi.mcsn.pad.core.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import voldemort.versioning.Occurred;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Versioned;

public class StorageManager {
	
	private ConcurrentMap <String, Versioned<String>> primaryMap ;
	private ConcurrentMap <String, Versioned<String>> backupMap ;
	private DB db;
	private VectorClock vc;	
	private int nid;	
	
	//TODO: only for testing
	public int getNid() {
		return nid;
	}

	public StorageManager(String id, VectorClock vc) {
		db = DBMaker.fileDB("file" + id + ".db").fileMmapEnable().make();
		primaryMap = (ConcurrentMap<String, Versioned<String>>) db.hashMap("myMap").createOrOpen();
		backupMap = (ConcurrentMap<String, Versioned<String>>) db.hashMap("backupMap").createOrOpen();
		this.vc = vc;		
		this.nid = Integer.parseInt(id);
	}	

	public boolean store(String surl, Versioned<String> versioned)
	{
		try{			
			vc.incremented(nid, System.currentTimeMillis());
			Versioned<String> updatedVersioned= new Versioned<String>(versioned.getValue(), vc);
			primaryMap.put(surl, updatedVersioned);			
			db.commit();
			return true;
		}
		catch(Exception e){
			db.rollback();
			return false;
		}		
	}
	
	
	public boolean store(String surl, Versioned<String> versioned, VectorClock vck)
	{
		try{			
			incrementAndMergeVc(vck);
			Versioned<String> updatedVersioned= new Versioned<String>(versioned.getValue(), vc);
			primaryMap.put(surl, updatedVersioned);			
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
		return store(surl, versioned, nmsg.getVectorClock());
	}
	
	
	public Versioned<String> read(String surl)
	{
		try{
			vc.incrementVersion(nid, System.currentTimeMillis());
			Versioned<String> read = primaryMap.get(surl);			
			db.commit();
			return read; 		
		}
		catch (Exception e) {
			db.rollback();
			return null;
		}		
	}
	
	public Versioned<String> read(String surl, VectorClock vck)
	{
		try{
			incrementAndMergeVc(vck);
			Versioned<String> read = primaryMap.get(surl);			
			db.commit();
			return read; 		
		}
		catch (Exception e) {
			db.rollback();
			return null;
		}		
	}
	
	public Versioned<String> read(NodeMessage nmsg)
	{
		try{
			String surl = nmsg.getShortUrl();
			Versioned<String> read = read(surl, nmsg.getVectorClock());			
			db.commit();
			return read; 		
		}
		catch (Exception e) {
			db.rollback();
			return null;
		}		
	}	

	
	public Versioned<String> remove(String surl){
		try{	
			vc.incrementVersion(nid, System.currentTimeMillis());
			Versioned<String> removed = primaryMap.remove(surl);			
			db.commit();
			return removed;
		}
		catch (Exception e){
			db.rollback();
			return null;
		}		
	}
	
	public Versioned<String> remove(String surl, VectorClock vck){
		try{					
			incrementAndMergeVc(vck);
			Versioned<String> removed = primaryMap.remove(surl);			
			db.commit();
			return removed;
		}
		catch (Exception e){
			db.rollback();
			return null;
		}		
	}
	
	public Versioned<String> remove(NodeMessage nmsg){
		try{
			String surl = nmsg.getShortUrl();			
			Versioned<String> removed = remove(surl, nmsg.getVectorClock());			
			db.commit();
			return removed;
		}
		catch (Exception e){
			db.rollback();
			return null;
		}		
	}
	
	
	public boolean storeWithConflictResolution(String surl, Versioned<String> versioned)
	{
		try{		
			Versioned<String>  versionedStored = primaryMap.get(surl);
			VectorClock vc1 = (VectorClock)versioned.getVersion();
			VectorClock vc2 = (VectorClock)versionedStored.getVersion(); 
			Occurred compared = vc1.compare(vc2);
			if(compared == Occurred.BEFORE)
				store(surl, versioned);
	// If the winning version is the already stored one there is nothing to do
			else if(compared == Occurred.CONCURRENTLY){
				long ts1 = vc1.getTimestamp();
				long ts2 = vc2.getTimestamp();
				if (ts1 > ts2)
					store(surl, versioned);
			}						
			db.commit();
			return true;
		}
		catch(Exception e){
			db.rollback();
			return false;
		}		
	}		
	
	public boolean contains(String surl)
	{		
		vc.incrementVersion(nid, System.currentTimeMillis());
			boolean contained = primaryMap.containsKey(surl);		
			db.commit();
			return contained; 				
	}
	
	
	public VectorClock getVc() {
		return vc;
	}


	//TODO method used only for testing
	public Versioned<String> readBackup(String surl)
	{
		try{
			Versioned<String> read = backupMap.get(surl);			
			db.commit();
			return read; 		
		}
		catch (Exception e) {
			db.rollback();
			return null;
		}		
	}		
	
	public Versioned<String> removeBackup(String surl){
		try{				
			Versioned<String> removed = backupMap.remove(surl);			
			db.commit();
			return removed;
		}
		catch (Exception e){
			db.rollback();
			return null;
		}		
	}
		
	
	/**	 
	 * @return a dump of the primary database
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
	
	/**	 
	 * @return a dump of the backup database
	 */
	public Map<String,Versioned<String>> getBackupDump() 
	{
		Map<String, Versioned<String>> dump = new HashMap<>();
		for(Entry<String, Versioned<String>> e :backupMap.entrySet()){
			String surl= e.getKey();
			// I deliberately create a copy of the object, that I will return
			Versioned<String> vlurl = new Versioned<String>(e.getValue().getValue(), e.getValue().getVersion());
			dump.put(surl, vlurl);
		}
		return dump;
	}
	
	public boolean storeBackup(String surl, Versioned<String> versioned)
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
			   store(surl,vlurl);			  
			   //System.out.println("node " + nid + "has stored into the primary the url " +  vlurl.getValue());			   
		   }
	   	return true;
		}   		
		catch(IllegalStateException e){
			return false;
		}
	}
	
	/*public void removeAlsoFromBackup(List<String> removed){
		if (removed != null){
			for(int i=0 ; i < removed.size() ; i++)
				try{				
					backupMap.remove(removed.get(i));			
					db.commit();					
				}
				catch (Exception e){
					db.rollback();					
				}						
		}
	}*/
	
	public boolean emptyBackup(){
		try{
			backupMap.clear();
			return true;
		}
		catch (UnsupportedOperationException e){
			return false;
		}
	}
	
	// Only used for test purposes
	public boolean emptyPrimary(){
		try{
			primaryMap.clear();
			return true;
		}
		catch (UnsupportedOperationException e){
			return false;
		}
	}
	
	public void incrementAndMergeVc(VectorClock received)
	{		
		vc.incrementVersion(nid, System.currentTimeMillis());
		vc.merge(received);	
	}	
	
	public void shutdown(){
		db.close();
	}
}
