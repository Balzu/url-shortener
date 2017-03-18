package it.unipi.mcsn.pad.core.storage;

import it.unipi.mcsn.pad.core.Service;
import voldemort.versioning.VectorClock;

public class StorageService implements Service{
	
	private StorageManager storageManager;
	private VectorClock vectorClock;
	
	public StorageService(VectorClock vc){
		
		vectorClock = vc;
	}
	
	public void store(){
		storageManager.store();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
