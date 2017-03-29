package it.unipi.mcsn.pad.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import com.google.common.hash.Hashing;

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
	 
	 public static Object deserialize(byte[] data) throws ClassNotFoundException, IOException{
		 ByteArrayInputStream in = new ByteArrayInputStream(data);	
			ObjectInputStream is = new ObjectInputStream(in);			
			return is.readObject();
	 }
	 
	 //TODO: returns the ASCII representation of the bytes, possibly including also
	 // invalid characters for a url
	 public static String generateShortUrl (String longUrl){
		 String prefix = "pad.ly/";
		 byte [] bid = Hashing.murmur3_32().hashString(longUrl, 
				 StandardCharsets.US_ASCII).asBytes();
		 String id = new String(bid, StandardCharsets.US_ASCII);
		 return prefix + id;
	 }

}
