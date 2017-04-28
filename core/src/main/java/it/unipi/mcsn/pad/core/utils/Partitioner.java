package it.unipi.mcsn.pad.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.google.code.gossip.LocalGossipMember;

import it.unipi.mcsn.pad.consistent.ConsistentHasher;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.BytesConverter;
import it.unipi.mcsn.pad.consistent.ConsistentHasher.HashFunction;
import it.unipi.mcsn.pad.consistent.ConsistentHasherImpl;

public class Partitioner <B,M>{

	
	private ConsistentHasher <B,M> consistentHasher;
	
	
	public Partitioner(final int virtualInstancesPerBucket,
			final BytesConverter<B> bucketDataToBytesConverter,
			final BytesConverter<M> memberDataToBytesConverter,
			final HashFunction hashFunction,
			List<B> buckets)
	{
		consistentHasher = new ConsistentHasherImpl<>(
				virtualInstancesPerBucket,
				bucketDataToBytesConverter,
				memberDataToBytesConverter,
				hashFunction
				);	
		
		initBuckets(buckets);
	}
	

	
	public B findPrimary(M key, List<B> buckets) 
	{		
		/*List<B> allBuckets = consistentHasher.getAllBuckets();		
		if (allBuckets.isEmpty()){
			initBuckets(buckets);
		}
		*/
		B targetBucket = consistentHasher.findBucket(key);
		if (buckets.contains(targetBucket))
			return targetBucket;
		
		NavigableMap<B, B> upBucketsMap = new ConcurrentSkipListMap<>();
		for(int i=0; i<buckets.size(); i++)
	    // Map is redundant actually, I only use it for its capability to return the "ceiling" of the key
			upBucketsMap.put(buckets.get(i), buckets.get(i)); 
		B inChargeBucket = upBucketsMap.ceilingKey(targetBucket);
		return inChargeBucket!= null ? inChargeBucket
				: upBucketsMap.firstKey();
	}
		
	
	
	
	private void initBuckets(List<B> buckets){		
			
		for (B bucket : buckets)
			consistentHasher.addBucket(bucket);	
	}
	

	// Check this method: it's naif, but should work because in practice B = int
	//set public only to test, should be private
	public boolean areEqual(List<B> oldB,List<B> newB){
		if (oldB.size() != newB.size())
			return false;
		for (int i=0; i< newB.size(); i++){
			if (oldB.get(i) != newB.get(i))
				return false;
		}
		return true;		
	}
	
	// Only for test purposes
	public ConsistentHasher<B, M> getConsistentHasher() {
		return consistentHasher;
	}

}
