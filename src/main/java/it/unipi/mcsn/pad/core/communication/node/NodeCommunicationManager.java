package it.unipi.mcsn.pad.core.communication.node;

import java.util.concurrent.atomic.AtomicBoolean;

import it.unipi.mcsn.pad.consistent.ConsistentHasher;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.BytesConverter;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.HashFunction;
import it.unipi.mcsn.pad.core.utils.Partitioner;
import voldemort.versioning.Versioned;


public class NodeCommunicationManager {
	
	private final AtomicBoolean nodeServiceRunning; 
	private ReplicaManager replicaManager;
	private RequestManager requestManager;
	// I identify the buckets(= nodes) with a unique integer (node id) 
	// and the members(=long_urls) with a String (their content).
	private Partitioner<Integer, String> partitioner;
	
	
	public NodeCommunicationManager() 
	{
		this(700, ConsistentHasher.SHA1);
	}
	
	public NodeCommunicationManager(final int virtualInstancesPerBucket,			
			final HashFunction hashFunction)
	{
		nodeServiceRunning = new AtomicBoolean(true);
		replicaManager = new ReplicaManager();
		requestManager = new RequestManager();
		partitioner = new Partitioner<>(virtualInstancesPerBucket,
				ConsistentHasher.getIntegerToBytesConverter(), 
				ConsistentHasher.getStringToBytesConverter(), hashFunction);
	}

}
