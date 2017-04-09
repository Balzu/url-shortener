package it.unipi.mcsn.pad.core.storage;

import it.unipi.mcsn.pad.core.Service;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import voldemort.versioning.VectorClock;

public class StorageService implements Service{
	
	private StorageManager storageManager;
	private VectorClock vectorClock;
	
	public StorageService(VectorClock vc, String id)
	{
		storageManager = new StorageManager(id);
		vectorClock = vc;
	}
	
	public StorageManager getStorageManager() 
	{
		return storageManager;
	}

	@Override
	public void start() {		
		// StorageManager already inizialized by the costructor
	}

	@Override
	public void shutdown() {
		storageManager.shutdown();		
	}

}
