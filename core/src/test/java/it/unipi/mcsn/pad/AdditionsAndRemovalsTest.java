package it.unipi.mcsn.pad;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;

import it.unipi.mcsn.pad.core.Node;
import it.unipi.mcsn.pad.core.NodeRunner;

public class AdditionsAndRemovalsTest {
	
	private static List<Node> nodes;
	private static List<Integer> backupIntervals;
	
	@Before
	public  void setupCluster(){		
		try {
			List<Integer> virtualInstances=null;	    	
	    	File configFile = new File ("src/main/resources/core.conf");
	    	List<String> addresses;
			addresses = NodeRunner.getAddressesFromFile(configFile);
			virtualInstances = NodeRunner.getVirtualInstancesFromFile(configFile);
			backupIntervals =  NodeRunner.getBackupIntervalsFromFile(configFile);
			Map<String, Integer> confs = NodeRunner.getConfFromFile(configFile);
    		int gossipPort = confs.get("gossip_port");
    		int clientPort = confs.get("client_port");
    		int nodePort = confs.get("node_port");    		
    		int gossipInterval = confs.get("gossip_interval");
    		int cleanupInterval = confs.get("cleanup_interval");
    		GossipSettings settings = new GossipSettings(gossipInterval, cleanupInterval); 			
			List<GossipMember> startupMembers = new ArrayList<>();
			for (int i = 0; i < addresses.size(); ++i) {
				startupMembers.add(new RemoteGossipMember(addresses.get(i), gossipPort, i + "")); //TODO: fix the id
			}			
			nodes = new ArrayList<>();
			for (int i = 0; i < addresses.size(); ++i) {	    			
				Node node = new Node(clientPort, 50, addresses.get(i), gossipPort, i+"" , 
						LogLevel.DEBUG,	startupMembers, settings, null,  i, nodePort,
						virtualInstances.get(i), backupIntervals.get(i));
				node.start();
				nodes.add(node);   
				Thread.sleep(20000);
			} 
		}		
		catch (IOException e) {			
			e.printStackTrace();
		} catch (JSONException e) {		
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}	
	
	
	@After
	public  void tearDownCluster(){
		for (Node node : nodes){
			node.getStorageService().getStorageManager().emptyPrimary();
			node.getStorageService().getStorageManager().emptyBackup();
			node.shutdown();			
		}
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void dummyTest(){
		
	}

}
