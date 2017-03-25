package it.unipi.mcsn.pad.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;

public class Utils {
	
	public static int getIntegerIpAddress (String ipAddress) throws NumberFormatException
	{		
		StringTokenizer tokenizer = new StringTokenizer(ipAddress, ".");
		StringBuffer stringId=null;
		while(tokenizer.hasMoreTokens())
			stringId.append(tokenizer.nextToken());
		return Integer.parseInt(stringId.toString());		
	}
	
	/**
	 * Returns the byte representation of an arbitrary object
	 * @throws IOException
	 */
	 public static byte[] serialize(Object obj) throws IOException {
	        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
	            try(ObjectOutputStream o = new ObjectOutputStream(b)){
	                o.writeObject(obj);
	            }
	            return b.toByteArray();
	        }
	    }

}
