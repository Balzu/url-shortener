package it.unipi.mcsn.pad.core.utils;

import java.util.StringTokenizer;

public class Utils {
	
	public static int getIntegerIpAddress (String ipAddress) throws NumberFormatException{
		
		StringTokenizer tokenizer = new StringTokenizer(ipAddress, ".");
		StringBuffer stringId=null;
		while(tokenizer.hasMoreTokens())
			stringId.append(tokenizer.nextToken());
		return Integer.parseInt(stringId.toString());		
	}

}
