package it.unipi.mcsn.pad.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;


public class NodeRunner 
{
    public static void main( String[] args )
    {     	
    	try {
    		GossipSettings settings = new GossipSettings();
    		int seedNodes = 5;
    		int port = 2000;
    		List<GossipMember> startupMembers = new ArrayList<>();
    		for (int i = 1; i < seedNodes+1; ++i) {
    			startupMembers.add(new RemoteGossipMember("127.0.0." + i, port, i + "")); //TODO: fix the id
    		}
    		
    		List<Node> nodes = new ArrayList<>();
    		for (int i = 1; i < seedNodes+1; ++i) {	    			
    			Node node = new Node(2001, 50, "127.0.0." + i, port, i+"" , LogLevel.DEBUG,
    					startupMembers, settings, null,  i);
    			node.start();
    			nodes.add(node);    				   
    		}       		

    	}catch (UnknownHostException e) {			
    		e.printStackTrace();
    	} catch (InterruptedException e) {		
    		e.printStackTrace();
    	}

    }
    
    private List<String> getAddressesFromFile(File configFile) throws IOException, JSONException {		
		
    	List<String> addresses = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(configFile));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null)
			buffer.append(line.trim());
		br.close();
		
		JSONObject jsonObject = new JSONArray(buffer.toString()).getJSONObject(0);
		JSONArray seeds = jsonObject.getJSONArray("members");
		
		String ipAddr;			
		for (int i = 0; i < seeds.length(); i++){
			ipAddr = seeds.getJSONObject(i).getString("host");			
			addresses.add(ipAddr);
		}
		return addresses;
    }
    
    private Map<String, Integer> getPortsFromFile(File configFile) throws IOException, JSONException{
    	
    	Map<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(configFile));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null)
			buffer.append(line.trim());
		br.close();
		
		JSONObject jsonObject = new JSONArray(buffer.toString()).getJSONObject(0);
		int gossipPort = jsonObject.getInt("gossip_port");
		int clientPort = jsonObject.getInt("client_port");
		int nodePort = jsonObject.getInt("node_port");
		map.put("gossip_port", gossipPort);
		map.put("client_port", clientPort);
		map.put("node_port", nodePort);
		
		return map;
    }
}
