package it.unipi.mcsn.pad.core.utils;

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
	
	
	
	
	public B findPrimary(M key) 
	{		
		return consistentHasher.findBucket(key);
	}

}
