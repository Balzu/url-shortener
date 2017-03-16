package it.unipi.mcsn.pad.core.storage;

import it.unipi.mcsn.pad.core.Service;

public class StorageService implements Service{
	
	private StorageManager storageManager;
	
	public StorageService(){
		
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
