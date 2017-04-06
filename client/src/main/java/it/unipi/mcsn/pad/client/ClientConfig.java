package it.unipi.mcsn.pad.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A configuration object that holds configuration parameters for the client.
 */
public class ClientConfig {
	
	private List<InetSocketAddress> addresses;
	
	public ClientConfig(File configFile) throws IOException, JSONException {
		addresses = new ArrayList<InetSocketAddress>();
		getAddressesFromFile(configFile);
	}
	
	private void getAddressesFromFile(File configFile) throws IOException, JSONException {		
		
			BufferedReader br = new BufferedReader(new FileReader(configFile));
			StringBuffer buffer = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null)
				buffer.append(line.trim());
			br.close();
			
			JSONObject jsonObject = new JSONArray(buffer.toString()).getJSONObject(0);
			JSONArray seeds = jsonObject.getJSONArray("members");
			
			
			
			//JSONObject obj = new JSONObject(configFile);
			//JSONArray seeds = obj.getJSONArray("members");
			String ipAddr;
			int port;
			InetSocketAddress address;
			for (int i = 0; i < seeds.length(); i++){
				ipAddr = seeds.getJSONObject(i).getString("host");
				port = seeds.getJSONObject(i).getInt("port");
				address = new InetSocketAddress(ipAddr, port);
				addresses.add(address);
			}
		
		
	//TODO: add operations that retrieve all the current nodes alive in the cluster?
		// Should be done in background spanning a thread?
		
		
	}

	public List<InetSocketAddress> getAddresses() {
		return addresses;
	}

}
