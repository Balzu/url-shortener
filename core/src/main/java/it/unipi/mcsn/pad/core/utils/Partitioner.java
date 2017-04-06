package it.unipi.mcsn.pad.core.utils;

import java.util.List;

import it.unipi.mcsn.pad.consistent.ConsistentHasher;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.BytesConverter;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.HashFunction;
import it.unipi.mcsn.pad.consistent.ConsistentHasherImpl;

public class Partitioner <B,M>{

	
	private ConsistentHasher <B,M> consistentHasher;
	
	/*
	public Partitioner(
			final BytesConverter<B> bucketDataToBytesConverter,
			final BytesConverter<M> memberDataToBytesConverter)
	{
		this(700, bucketDataToBytesConverter, 
				memberDataToBytesConverter, ConsistentHasher.SHA1);
	}
	*/
	
	public Partitioner(final int virtualInstancesPerBucket,
			final BytesConverter<B> bucketDataToBytesConverter,
			final BytesConverter<M> memberDataToBytesConverter,
			final HashFunction hashFunction)
	{
		consistentHasher = new ConsistentHasherImpl<>(
				virtualInstancesPerBucket,
				bucketDataToBytesConverter,
				memberDataToBytesConverter,
				hashFunction
				);		
	}
	
	
	
	
	public B findPrimary(M key, List<B> buckets) 
	{		
		List<B> oldBuckets = consistentHasher.getAllBuckets();
		// If the active nodes are different than the previous nodes in the bucket,
		// I remove all the old nodes and insert the new ones
		if (!areEqual(oldBuckets, buckets)){
			for (B bucket : oldBuckets)
				try {
					consistentHasher.removeBucket(bucket);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			for (B bucket : buckets)
				consistentHasher.addBucket(bucket);					
		}
		
		return consistentHasher.findBucket(key);
	}
	
	// Check this method: it's naif, but should work because in practice B = int
	private boolean areEqual (List<B> oldB,List<B> newB){
		if (oldB.size() != newB.size())
			return false;
		for (int i=0; i< newB.size(); i++){
			if (oldB.get(i) != newB.get(i))
				return false;
		}
		return true;		
	}

}
