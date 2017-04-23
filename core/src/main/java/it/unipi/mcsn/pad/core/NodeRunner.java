package it.unipi.mcsn.pad.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
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
    		CoreCommandLineManager clm = new CoreCommandLineManager(args); 
			
			// If asked help, show help message and quit
    		//TODO if --help and other options are present it does not return usage error message, 
    		// but just prints help message and quit - think it's ok, maven too behaves so
			if (clm.needHelp()){ 
				clm.printHelp();
				System.exit(0);
			}
    		
			File configFile;
			if (clm.hasConfigFile()){
				String path = clm.getConfigurationPath();
				configFile = new File ( path + "/client.conf");
			}
			else{
				configFile = new File ("src/main/resources/core.conf");
			}
			System.out.println("Configuration file exists? " + configFile.exists()); // TODO throw exception if config file does not exist?
			
    		List<String> addresses =  getAddressesFromFile(configFile);
    		List<Integer> virtualInstances = getVirtualInstancesFromFile(configFile);
    		List<Integer> backupIntervals =  getBackupIntervalsFromFile(configFile);
    		Map<String, Integer> ports = getPortsFromFile(configFile);
    		int gossipPort = ports.get("gossip_port");
    		int clientPort = ports.get("client_port");
    		int nodePort = ports.get("node_port");
    		
    		    		
    		GossipSettings settings = new GossipSettings();
    		
    		
    		List<GossipMember> startupMembers = new ArrayList<>();
    		for (int i = 0; i < addresses.size(); ++i) {
    			startupMembers.add(new RemoteGossipMember(addresses.get(i), gossipPort, i + "")); //TODO: fix the id
    		}
    		
    		List<Node> nodes = new ArrayList<>();
    		for (int i = 0; i < addresses.size(); ++i) {	    			
    			Node node = new Node(clientPort, 50, addresses.get(i), gossipPort, i+"" , 
    					LogLevel.DEBUG,	startupMembers, settings, null,  i, nodePort,
    					virtualInstances.get(i), backupIntervals.get(i));
    			node.start();
    			nodes.add(node);    				   
    		}       	
    		
    		System.out.println("url-shortener service is running... ");
    		BufferedReader stdIn = new BufferedReader(
					new InputStreamReader(System.in));	
		    String input;
    		while (true) {    			
    			displayUsageMessage();
    			input = stdIn.readLine();
    			if (input.equals("quit")){
    				System.out.println("Shutting down the service... ");
    				for (Node node : nodes){
    					node.shutdown();
    				}
    				System.exit(0);
    			}
    		}

    	}catch (UnknownHostException e) {			
    		e.printStackTrace();
    	} catch (InterruptedException e) {		
    		e.printStackTrace();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {			
    		System.err.println(e.getMessage());
    		System.out.println("Run with \"--help\" to get usage information about command line");
		}
    }
    
    public static List<String> getAddressesFromFile(File configFile) throws IOException, JSONException {		
		
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
    
 public static List<Integer> getVirtualInstancesFromFile(File configFile) throws IOException, JSONException {		
		
    	List<Integer> virtualInstances = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(configFile));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null)
			buffer.append(line.trim());
		br.close();
		
		JSONObject jsonObject = new JSONArray(buffer.toString()).getJSONObject(0);
		JSONArray seeds = jsonObject.getJSONArray("members");
		
		String virtInst;			
		for (int i = 0; i < seeds.length(); i++){
			virtInst = seeds.getJSONObject(i).getString("virtual_instances");			
			virtualInstances.add(Integer.parseInt(virtInst));
		}
		return virtualInstances;
    }
 
 public static List<Integer> getBackupIntervalsFromFile(File configFile) throws IOException, JSONException {		
		
 	List<Integer> backupIntervals = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(configFile));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null)
			buffer.append(line.trim());
		br.close();
		
		JSONObject jsonObject = new JSONArray(buffer.toString()).getJSONObject(0);
		JSONArray seeds = jsonObject.getJSONArray("members");
		
		String backInt;			
		for (int i = 0; i < seeds.length(); i++){
			backInt = seeds.getJSONObject(i).getString("backup_interval");			
			backupIntervals.add(Integer.parseInt(backInt));
		}
		return backupIntervals;
 }
 
 
    
    public static Map<String, Integer> getPortsFromFile(File configFile) throws IOException, JSONException{
    	
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
    
    public static void displayUsageMessage(){
		System.out.println("Usage: \n" +
				"- quit                 to quit the service \n");
	}
}
